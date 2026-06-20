package com.ftn.service.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.time.SessionPseudoClock;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.ftn.model.FloodRiskAssessment;
import com.ftn.model.FlowRateStatus;
import com.ftn.model.InterventionRecommendation;
import com.ftn.model.Location;
import com.ftn.model.PumpOperationalStatus;
import com.ftn.model.Sensor;
import com.ftn.model.StationCapacity;
import com.ftn.model.SystemAlert;
import com.ftn.model.ThresholdConfig;
import com.ftn.model.TrendData;
import com.ftn.model.WaterLevelStatus;
import com.ftn.model.enums.LocationType;
import com.ftn.model.enums.ParameterType;
import com.ftn.model.enums.PumpEventType;
import com.ftn.model.enums.PumpState;
import com.ftn.model.enums.SensorType;
import com.ftn.model.events.ConnectionLostAlert;
import com.ftn.model.events.HeartbeatEvent;
import com.ftn.model.events.PumpConnectionLostAlert;
import com.ftn.model.events.PumpEvent;
import com.ftn.model.events.PumpFailureAlert;
import com.ftn.model.events.RapidWaterLevelRise;
import com.ftn.model.events.SensorReadingEvent;
import com.ftn.service.dto.monitoring.MonitoringEventDTO;
import com.ftn.service.dto.monitoring.MonitoringLocationDTO;
import com.ftn.service.dto.monitoring.MonitoringStatusDTO;
import com.ftn.service.dto.monitoring.MonitoringTickDTO;
import com.ftn.service.repository.LocationRepository;
import com.ftn.service.repository.SensorRepository;
import com.ftn.service.repository.ThresholdConfigRepository;
import com.ftn.service.repository.TrendDataRepository;
import com.ftn.service.repository.WeatherConditionRepository;
import com.ftn.service.utils.Helper;
import com.ftn.service.utils.SensorValueRange;
import com.ftn.service.utils.Trend;

@Service
public class RealTimeSimulationService {

    private static final int TICK_SECONDS = 5;

    private static final int PSEUDO_STEP_MINUTES = 3;

    private static final double TREND_START_CHANCE = 0.10;
    private static final double RISE_BIAS = 0.70;
    private static final int TREND_MIN_TICKS = 3;
    private static final int TREND_EXTRA_TICKS = 3;

    private static final int DEMO_PUMP_FAULTY_TICK = 2;    // Rule 6  pump goes FAULTY
    private static final int DEMO_PUMP_RECOVER_TICK = 3;   // Rule 7  pump back to ACTIVE -> alert cleared
    private static final int DEMO_STALE_STATUS_TICK = 4;   // Rule 5  stale duplicate status cleaned up
    private static final int DEMO_RESTART_FROM_TICK = 5;   // Rule 2  pump restart burst...
    private static final int DEMO_RESTART_TO_TICK = 8;     //         ...4 restarts within the hour
    private static final int DEMO_HB_GAP_FROM_TICK = 9;    // Rule 3  location stops sending heartbeats...
    private static final int DEMO_HB_GAP_TO_TICK = 10;     //         ...gap exceeds 5 min (Rule 8 clears at 11)
    private static final int DEMO_PUMP_GAP_FROM_TICK = 12; // Rule 4  pump stops reporting...
    private static final int DEMO_PUMP_GAP_TO_TICK = 15;   //         ...gap exceeds 10 min (Rule 9 clears at 16)

    private static final AgendaFilter NO_CEP =
            match -> !match.getRule().getName().startsWith("CEP");

    private final LocationRepository locationRepository;
    private final SensorRepository sensorRepository;
    private final ThresholdConfigRepository thresholdConfigRepository;
    private final WeatherConditionRepository weatherConditionRepository;
    private final TrendDataRepository trendDataRepository;
    private final RuleTemplateService ruleTemplateService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Random random = new Random();
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "realtime-monitoring");
                t.setDaemon(true);
                return t;
            });

    private volatile boolean active = false;
    private boolean cepEnabled = false;
    private int tickCount = 0;
    private LocalDateTime pseudoTime;

    private KieSession session;
    private SessionPseudoClock clock;
    private ScheduledFuture<?> task;

    private List<Location> locations = new ArrayList<>();
    private List<Sensor> sensors = new ArrayList<>();
    private List<ThresholdConfig> thresholds = new ArrayList<>();
    private final Map<String, FactHandle> readingHandles = new HashMap<>();
    private final Map<String, Double> values = new HashMap<>();

    private final Map<String, String> previousRisk = new HashMap<>();
    private String previousSystemAlert;
    private Set<String> previousCepAlerts = new HashSet<>();

    private final Map<String, ThresholdConfig> thresholdsByKey = new HashMap<>();
    private final Map<String, Trend> trends = new HashMap<>();

    private Sensor demoPump;
    private String demoSilentLocationCode;

    public RealTimeSimulationService(LocationRepository locationRepository,
                                     SensorRepository sensorRepository,
                                     ThresholdConfigRepository thresholdConfigRepository,
                                     WeatherConditionRepository weatherConditionRepository,
                                     TrendDataRepository trendDataRepository,
                                     RuleTemplateService ruleTemplateService,
                                     SimpMessagingTemplate messagingTemplate) {
        this.locationRepository = locationRepository;
        this.sensorRepository = sensorRepository;
        this.thresholdConfigRepository = thresholdConfigRepository;
        this.weatherConditionRepository = weatherConditionRepository;
        this.trendDataRepository = trendDataRepository;
        this.ruleTemplateService = ruleTemplateService;
        this.messagingTemplate = messagingTemplate;
    }

    public synchronized MonitoringStatusDTO start(boolean cep) {
        if (active) {
            doStop();
        }
        this.cepEnabled = cep;
        loadDomain();
        buildSession();
        this.pseudoTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        this.tickCount = 0;
        this.previousRisk.clear();
        this.previousSystemAlert = null;
        this.previousCepAlerts = new HashSet<>();
        this.active = true;
        this.task = scheduler.scheduleWithFixedDelay(
                this::safeTick, 0, TICK_SECONDS, TimeUnit.SECONDS);
        return status();
    }

    public synchronized MonitoringStatusDTO stop() {
        doStop();
        return status();
    }

    public synchronized MonitoringStatusDTO status() {
        MonitoringStatusDTO dto = new MonitoringStatusDTO();
        dto.setActive(active);
        dto.setCepEnabled(cepEnabled);
        dto.setTick(tickCount);
        dto.setPseudoTime(pseudoTime != null ? pseudoTime.toString() : null);
        dto.setLocationCount(locations.size());
        dto.setTickIntervalSeconds(TICK_SECONDS);
        dto.setPseudoStepMinutes(PSEUDO_STEP_MINUTES);
        return dto;
    }

    private void doStop() {
        active = false;
        if (task != null) {
            task.cancel(false);
            task = null;
        }
        if (session != null) {
            try {
                session.dispose();
            } catch (RuntimeException e) {
                System.err.println("Error disposing monitoring session: " + e.getMessage());
            }
            session = null;
        }
        clock = null;
        readingHandles.clear();
        values.clear();
        trends.clear();
        thresholdsByKey.clear();
    }

    private void loadDomain() {
        locations = new ArrayList<>();
        for (Location l : locationRepository.findAll()) {
            if (l.isActive() && l.getPosX() != null && l.getPosY() != null) {
                locations.add(l);
            }
        }
        Set<Long> activeLocationIds = new HashSet<>();
        for (Location l : locations) {
            activeLocationIds.add(l.getId());
        }
        sensors = new ArrayList<>();
        for (Sensor s : sensorRepository.findAll()) {
            if (s.getLocation() != null && activeLocationIds.contains(s.getLocation().getId())) {
                sensors.add(s);
            }
        }
        reloadThresholdMap();

        values.clear();
        for (Sensor s : sensors) {
            double initial = s.getSensorType() == SensorType.PUMP_STATUS
                    ? 1.0 : valueRangeFor(s).getBaseline();
            values.put(Helper.sensorKey(s), initial);
        }

        // for RapidWaterLevelRise
        trends.clear();
        for (Sensor s : sensors) {
            if (s.getSensorType() == SensorType.WATER_LEVEL) {
                trends.put(Helper.sensorKey(s), new Trend(1, TREND_MIN_TICKS + 1));
                break;
            }
        }

        chooseDemoTargets();
    }

    // Select one pump and one other location to drive the CEP demo script. The pump will be forced to go FAULTY for one tick, then healthy again. The other location will have its heartbeat muted for a few ticks.
    private void chooseDemoTargets() {
        demoPump = null;
        for (Sensor s : sensors) {
            if (s.getSensorType() == SensorType.PUMP_STATUS) {
                demoPump = s;
                break;
            }
        }
        String pumpLocationCode = demoPump != null ? demoPump.getLocation().getCode() : null;
        demoSilentLocationCode = null;
        for (Location l : locations) {
            if (!l.getCode().equals(pumpLocationCode)) {
                demoSilentLocationCode = l.getCode();
                break;
            }
        }
        if (demoSilentLocationCode == null && !locations.isEmpty()) {
            demoSilentLocationCode = locations.get(0).getCode();
        }
    }

    private void buildSession() {
        KieBase kieBase = ruleTemplateService.buildStream(thresholds);
        KieSessionConfiguration conf = KieServices.Factory.get().newKieSessionConfiguration();
        conf.setOption(ClockTypeOption.get("pseudo"));
        session = kieBase.newKieSession(conf, null);
        clock = session.getSessionClock();

        clock.advanceTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        readingHandles.clear();
        Helper.seedFacts(session, thresholds, locations, weatherConditionRepository);
    }

    private void safeTick() {
        try {
            tick();
        } catch (Exception e) {
            System.err.println("Real-time monitoring tick failed: " + e.getMessage());
        }
    }

    private synchronized void tick() {
        if (!active || session == null) {
            return;
        }
        refreshThresholds();
        tickCount++;
        clock.advanceTime(PSEUDO_STEP_MINUTES, TimeUnit.MINUTES);
        pseudoTime = pseudoTime.plusMinutes(PSEUDO_STEP_MINUTES);
    
        Date eventTs = new Date(clock.getCurrentTime());
        Date factTs = new Date();

        Map<String, Location> byCode = new LinkedHashMap<>();
        for (Location l : locations) {
            byCode.put(l.getCode(), l);
            if (cepEnabled && !demoSkipHeartbeat(l.getCode())) {
                session.insert(new HeartbeatEvent(l.getCode(), eventTs));
            }
        }

        List<TrendData> persisted = new ArrayList<>();
        for (Sensor s : sensors) {
            Location loc = s.getLocation();
            double value = demoForcedValue(s);
            if (Double.isNaN(value)) {
                value = nextValue(s);
            }
            values.put(Helper.sensorKey(s), value);
            Helper.applyReading(session, readingHandles, loc, s.getSensorType(),
                    s.getTagName(), value, factTs);

            if (cepEnabled && !demoSkipPumpEvent(s)) {
                session.insert(new SensorReadingEvent(
                        loc.getCode(), s.getSensorType(), s.getTagName(), value, eventTs));
            }
            TrendData td = new TrendData();
            td.setLocationCode(loc.getCode());
            td.setTagName(s.getTagName());
            td.setLogTime(pseudoTime);
            td.setTagValue(value);
            persisted.add(td);
        }

        if (cepEnabled) {
            injectDemoFacts(eventTs);
        }

        int fired = cepEnabled ? session.fireAllRules() : session.fireAllRules(NO_CEP);

        try {
            trendDataRepository.saveAll(persisted);
        } catch (RuntimeException e) {
            System.err.println("Failed to persist trend data for tick " + tickCount + ": " + e.getMessage());
        }

        MonitoringTickDTO payload = buildPayload(byCode, fired);
        messagingTemplate.convertAndSend("/topic/monitoring", payload);
    }

    private MonitoringTickDTO buildPayload(Map<String, Location> byCode, int fired) {
        Map<String, WaterLevelStatus> wls =
                Helper.index(session, WaterLevelStatus.class, WaterLevelStatus::getLocation);
        Map<String, FlowRateStatus> frs =
                Helper.index(session, FlowRateStatus.class, FlowRateStatus::getLocation);
        Map<String, StationCapacity> caps =
                Helper.index(session, StationCapacity.class, StationCapacity::getLocation);
        Map<String, FloodRiskAssessment> risks =
                Helper.index(session, FloodRiskAssessment.class, FloodRiskAssessment::getLocation);
        Map<String, InterventionRecommendation> recs =
                Helper.index(session, InterventionRecommendation.class, InterventionRecommendation::getLocation);

        SystemAlert alert = Helper.latestSystemAlert(session);
        String alertLevel = alert != null ? alert.getLevel().name() : null;

        MonitoringTickDTO payload = new MonitoringTickDTO();
        payload.setTick(tickCount);
        payload.setPseudoTime(pseudoTime.toString());
        payload.setCepEnabled(cepEnabled);
        payload.setFiredRules(fired);
        payload.setSystemAlertLevel(alertLevel);
        payload.setSystemAlertDescription(alert != null ? alert.getDescription() : null);

        List<MonitoringEventDTO> events = new ArrayList<>();
        String time = pseudoTime.toString();

        for (Map.Entry<String, Location> e : byCode.entrySet()) {
            String code = e.getKey();
            Location loc = e.getValue();
            MonitoringLocationDTO dto = new MonitoringLocationDTO();
            dto.setLocationCode(code);
            dto.setDisplayCode(loc.getDisplayCode());
            dto.setLocationType(loc.getType() != null ? loc.getType().name() : null);
            dto.setZoneCode(loc.getZone() != null ? loc.getZone().getCode() : null);
            dto.setPosX(loc.getPosX());
            dto.setPosY(loc.getPosY());

            WaterLevelStatus w = wls.get(code);
            if (w != null) {
                dto.setWaterLevel(w.getLevel() != null ? w.getLevel().name() : null);
                dto.setWaterValue(w.getValue());
            }
            FlowRateStatus f = frs.get(code);
            if (f != null) {
                dto.setFlowLevel(f.getLevel() != null ? f.getLevel().name() : null);
                dto.setFlowValue(f.getValue());
            }
            StationCapacity c = caps.get(code);
            if (c != null) {
                dto.setCapacityLevel(c.getLevel() != null ? c.getLevel().name() : null);
                dto.setActivePumps(c.getActivePumps());
                dto.setTotalPumps(c.getTotalPumps());
            }
            FloodRiskAssessment r = risks.get(code);
            if (r != null) {
                dto.setRiskLevel(r.getRiskLevel() != null ? r.getRiskLevel().name() : null);
                dto.setRiskReason(r.getReason());
            }
            InterventionRecommendation rec = recs.get(code);
            if (rec != null) {
                dto.setRecommendation(rec.getType() != null ? rec.getType().name() : null);
                dto.setRecommendationPriority(
                        rec.getPriority() != null ? rec.getPriority().name() : null);
                dto.setRecommendationDescription(rec.getDescription());
            }
            dto.setSeverity(Helper.severity(dto));
            payload.getLocations().add(dto);

            String newRisk = dto.getRiskLevel();
            String oldRisk = previousRisk.put(code, newRisk == null ? "" : newRisk);
            if (!Objects.equals(oldRisk == null ? "" : oldRisk, newRisk == null ? "" : newRisk)
                    && newRisk != null) {
                events.add(new MonitoringEventDTO(time, dto.getSeverity(), code,
                        "Flood risk " + (oldRisk == null || oldRisk.isEmpty() ? "-" : oldRisk)
                                + " -> " + newRisk));
            }
        }

        if (!Objects.equals(previousSystemAlert, alertLevel) && alertLevel != null) {
            events.add(new MonitoringEventDTO(time, Helper.mapAlertSeverity(alertLevel), null,
                    "SYSTEM ALERT: " + (previousSystemAlert == null ? "-" : previousSystemAlert)
                            + " -> " + alertLevel));
        }
        previousSystemAlert = alertLevel;

        events.addAll(detectCepAlerts(time));

        payload.setEvents(events);
        return payload;
    }

    private List<MonitoringEventDTO> detectCepAlerts(String time) {
        List<MonitoringEventDTO> events = new ArrayList<>();
        Set<String> current = new HashSet<>();

        for (Object o : session.getObjects(obj -> obj instanceof RapidWaterLevelRise)) {
            RapidWaterLevelRise r = (RapidWaterLevelRise) o;
            String key = "RWL|" + r.getLocationCode();
            current.add(key);
            if (!previousCepAlerts.contains(key)) {
                events.add(new MonitoringEventDTO(time, "CRITICAL", r.getLocationCode(),
                        "CEP: rapid water-level rise +" + Math.round(r.getRiseCm())
                                + " cm in " + r.getPeriodMinutes() + " min"));
            }
        }
        for (Object o : session.getObjects(obj -> obj instanceof PumpFailureAlert)) {
            PumpFailureAlert p = (PumpFailureAlert) o;
            String key = "PFA|" + p.getLocationCode() + "|" + p.getPumpId();
            current.add(key);
            if (!previousCepAlerts.contains(key)) {
                events.add(new MonitoringEventDTO(time, "DANGER", p.getLocationCode(),
                        "CEP: " + p.getDescription()));
            }
        }
        for (Object o : session.getObjects(obj -> obj instanceof ConnectionLostAlert)) {
            ConnectionLostAlert c = (ConnectionLostAlert) o;
            String key = "CLA|" + c.getLocationCode();
            current.add(key);
            if (!previousCepAlerts.contains(key)) {
                events.add(new MonitoringEventDTO(time, "DANGER", c.getLocationCode(),
                        "CEP: " + c.getDescription()));
            }
        }
        for (Object o : session.getObjects(obj -> obj instanceof PumpConnectionLostAlert)) {
            PumpConnectionLostAlert c = (PumpConnectionLostAlert) o;
            String key = "PCLA|" + c.getLocationCode() + "|" + c.getPumpId();
            current.add(key);
            if (!previousCepAlerts.contains(key)) {
                events.add(new MonitoringEventDTO(time, "DANGER", c.getLocationCode(),
                        "CEP: " + c.getDescription()));
            }
        }

        previousCepAlerts = current;
        return events;
    }

    private double nextValue(Sensor s) {
        if (s.getSensorType() == SensorType.PUMP_STATUS) {
            return nextPumpValue();
        }

        String key = Helper.sensorKey(s);
        SensorValueRange range = valueRangeFor(s);
        double cur = values.getOrDefault(key, range.getBaseline());

        Trend trend = currentTrend(key);
        if (trend == null) {
            // calm behaviour: random walk that drifts back toward the normal value
            return Helper.meanRevert(random, cur, range.getBaseline(), range.getAmplitude(),
                    range.getMin(), range.getMax());
        }

        // active trend: push hard in one direction for a few ticks to drive escalations
        double noise = (random.nextDouble() - 0.5) * range.getAmplitude() * 0.4;
        double next = cur + trend.direction() * range.getStep() + noise;
        next = Math.max(range.getMin(), Math.min(range.getMax(), next));
        return Math.round(next * 10.0) / 10.0;
    }

    private double nextPumpValue() {
        double r = random.nextDouble();
        if (r < 0.80) {
            return 1.0;
        } else if (r < 0.90) {
            return 0.0;
        } else if (r < 0.95) {
            return -1.0;
        }
        return -2.0;
    }

    private Trend currentTrend(String key) {
        Trend trend = trends.get(key);
        if (trend != null && trend.isActive()) {
            trend.consumeTick();
            return trend;
        }
        if (random.nextDouble() < TREND_START_CHANCE) {
            int direction = random.nextDouble() < RISE_BIAS ? 1 : -1;
            int length = TREND_MIN_TICKS + random.nextInt(TREND_EXTRA_TICKS);
            Trend fresh = new Trend(direction, length);
            fresh.consumeTick();
            trends.put(key, fresh);
            return fresh;
        }
        trends.remove(key);
        return null;
    }

    private SensorValueRange valueRangeFor(Sensor s) {
        ParameterType pt = parameterType(s.getSensorType());
        ThresholdConfig tc = pt == null ? null
                : thresholdsByKey.get(thresholdKey(s.getLocation().getType(), pt));
        if (tc == null) {
            double base = Helper.baseline(s.getSensorType());
            return new SensorValueRange(base, base * 0.2, base * 0.5, base * 1.5, base * 0.2);
        }
        double normalMax = tc.getNormalMax();
        double warningMax = tc.getWarningMax();
        double criticalMax = tc.getCriticalMax() != null ? tc.getCriticalMax() : warningMax * 1.4;
        double min = normalMax * 0.25;
        double max = criticalMax * 1.25;
        double baseline = normalMax * 0.7;
        double amplitude = Math.max(10.0, (warningMax - normalMax) * 0.6);
        double step = (max - min) / 6.0;
        return new SensorValueRange(baseline, amplitude, min, max, step);
    }

    private void refreshThresholds() {
        reloadThresholdMap();
        for (Object o : session.getObjects(obj -> obj instanceof ThresholdConfig)) {
            ThresholdConfig fact = (ThresholdConfig) o;
            ThresholdConfig fresh = thresholdsByKey.get(
                    thresholdKey(fact.getLocationType(), fact.getParameterType()));
            if (fresh == null) {
                continue;
            }
            boolean changed = fact.getNormalMax() != fresh.getNormalMax()
                    || fact.getWarningMax() != fresh.getWarningMax()
                    || !Objects.equals(fact.getCriticalMax(), fresh.getCriticalMax());
            if (changed) {
                fact.setNormalMax(fresh.getNormalMax());
                fact.setWarningMax(fresh.getWarningMax());
                fact.setCriticalMax(fresh.getCriticalMax());
                session.update(session.getFactHandle(fact), fact);
            }
        }
    }

    private void reloadThresholdMap() {
        thresholds = thresholdConfigRepository.findAll();
        thresholdsByKey.clear();
        for (ThresholdConfig tc : thresholds) {
            thresholdsByKey.put(thresholdKey(tc.getLocationType(), tc.getParameterType()), tc);
        }
    }

    private static String thresholdKey(LocationType locationType, ParameterType parameterType) {
        return locationType + "|" + parameterType;
    }

    private static ParameterType parameterType(SensorType type) {
        switch (type) {
            case WATER_LEVEL:
                return ParameterType.WATER_LEVEL;
            case FLOW_RATE:
                return ParameterType.FLOW_RATE;
            default:
                return null;
        }
    }

    private double demoForcedValue(Sensor s) {
        if (!cepEnabled || s != demoPump) {
            return Double.NaN;
        }
        if (tickCount == DEMO_PUMP_FAULTY_TICK) {
            return -1.0; // FAULTY
        }
        if (tickCount >= DEMO_PUMP_RECOVER_TICK && tickCount <= DEMO_PUMP_GAP_TO_TICK + 1) {
            return 1.0;  // keep the pump healthy and predictable for the rest of the script
        }
        return Double.NaN;
    }

    // mute the demo location's heartbeat for a few ticks (gap > 5 min) to trigger the "connection lost" rule.
    private boolean demoSkipHeartbeat(String locationCode) {
        return cepEnabled
                && locationCode.equals(demoSilentLocationCode)
                && tickCount >= DEMO_HB_GAP_FROM_TICK
                && tickCount <= DEMO_HB_GAP_TO_TICK;
    }

    // mute the demo pump's heartbeat for a few ticks (gap > 10 min) to trigger the "pump connection lost" rule.
    private boolean demoSkipPumpEvent(Sensor s) {
        return cepEnabled
                && s == demoPump
                && tickCount >= DEMO_PUMP_GAP_FROM_TICK
                && tickCount <= DEMO_PUMP_GAP_TO_TICK;
    }

    private void injectDemoFacts(Date ts) {
        if (demoPump == null) {
            return;
        }
        Location loc = demoPump.getLocation();
        String pumpId = demoPump.getTagName();

        // insert pump operational status to delete the stale duplicate status
        if (tickCount == DEMO_STALE_STATUS_TICK) {
            Date older = new Date(System.currentTimeMillis() - 60_000L);
            session.insert(new PumpOperationalStatus(loc, pumpId, PumpState.ACTIVE, older));
        }

        // insert pump restart events to trigger the "too many restarts" rule
        if (tickCount >= DEMO_RESTART_FROM_TICK && tickCount <= DEMO_RESTART_TO_TICK) {
            session.insert(new PumpEvent(loc.getCode(), pumpId, PumpEventType.RESTART, ts));
        }
    }

}
