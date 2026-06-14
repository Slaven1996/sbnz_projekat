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
import java.util.function.Function;

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
import com.ftn.model.Sensor;
import com.ftn.model.SensorReading;
import com.ftn.model.StationCapacity;
import com.ftn.model.SystemAlert;
import com.ftn.model.ThresholdConfig;
import com.ftn.model.TrendData;
import com.ftn.model.WaterLevelStatus;
import com.ftn.model.WeatherCondition;
import com.ftn.model.enums.SensorType;
import com.ftn.model.events.ConnectionLostAlert;
import com.ftn.model.events.HeartbeatEvent;
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

@Service
public class RealTimeSimulationService {

    private static final int TICK_SECONDS = 5;

    private static final int PSEUDO_STEP_MINUTES = 3;

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
    private boolean cepEnabled = true;
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
        thresholds = thresholdConfigRepository.findAll();

        values.clear();
        for (Sensor s : sensors) {
            values.put(sensorKey(s), baseline(s.getSensorType()));
        }
    }

    private void buildSession() {
        KieBase kieBase = ruleTemplateService.buildStream(thresholds);
        KieSessionConfiguration conf = KieServices.Factory.get().newKieSessionConfiguration();
        conf.setOption(ClockTypeOption.get("pseudo"));
        session = kieBase.newKieSession(conf, null);
        clock = session.getSessionClock();

        readingHandles.clear();
        for (ThresholdConfig tc : thresholds) {
            session.insert(tc);
        }
        for (Location loc : locations) {
            session.insert(loc);
            weatherConditionRepository.findByLocationId(loc.getId()).ifPresent(
                    wc -> session.insert(new WeatherCondition(loc, wc.getPrecipitation())));
        }
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
        tickCount++;
        clock.advanceTime(PSEUDO_STEP_MINUTES, TimeUnit.MINUTES);
        pseudoTime = pseudoTime.plusMinutes(PSEUDO_STEP_MINUTES);
        Date ts = new Date(clock.getCurrentTime());

        Map<String, Location> byCode = new LinkedHashMap<>();
        for (Location l : locations) {
            byCode.put(l.getCode(), l);
            if (cepEnabled) {
                session.insert(new HeartbeatEvent(l.getCode(), ts));
            }
        }

        List<TrendData> persisted = new ArrayList<>();
        for (Sensor s : sensors) {
            Location loc = s.getLocation();
            double value = nextValue(s);
            values.put(sensorKey(s), value);
            applyReading(loc, s.getSensorType(), s.getTagName(), value, ts);
            if (cepEnabled) {
                session.insert(new SensorReadingEvent(
                        loc.getCode(), s.getSensorType(), s.getTagName(), value, ts));
            }
            TrendData td = new TrendData();
            td.setLocationCode(loc.getCode());
            td.setTagName(s.getTagName());
            td.setLogTime(pseudoTime);
            td.setTagValue(value);
            persisted.add(td);
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

    private void applyReading(Location loc, SensorType type, String tagName, double value, Date ts) {
        String handleKey = sensorKey(loc.getCode(), tagName);
        FactHandle handle = readingHandles.get(handleKey);
        if (handle == null) {
            SensorReading reading = new SensorReading(loc, type, tagName, value, ts);
            readingHandles.put(handleKey, session.insert(reading));
        } else {
            SensorReading reading = (SensorReading) session.getObject(handle);
            reading.setValue(value);
            reading.setTimestamp(ts);
            session.update(handle, reading);
        }
    }

    private MonitoringTickDTO buildPayload(Map<String, Location> byCode, int fired) {
        Map<String, WaterLevelStatus> wls = index(WaterLevelStatus.class, WaterLevelStatus::getLocation);
        Map<String, FlowRateStatus> frs = index(FlowRateStatus.class, FlowRateStatus::getLocation);
        Map<String, StationCapacity> caps = index(StationCapacity.class, StationCapacity::getLocation);
        Map<String, FloodRiskAssessment> risks = index(FloodRiskAssessment.class, FloodRiskAssessment::getLocation);
        Map<String, InterventionRecommendation> recs =
                index(InterventionRecommendation.class, InterventionRecommendation::getLocation);

        SystemAlert alert = null;
        for (Object o : session.getObjects(obj -> obj instanceof SystemAlert)) {
            alert = (SystemAlert) o;
        }
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
            dto.setSeverity(severity(dto));
            payload.getLocations().add(dto);

            String newRisk = dto.getRiskLevel();
            String oldRisk = previousRisk.put(code, newRisk == null ? "" : newRisk);
            if (!Objects.equals(oldRisk == null ? "" : oldRisk, newRisk == null ? "" : newRisk)
                    && newRisk != null) {
                events.add(new MonitoringEventDTO(time, dto.getSeverity(), code,
                        "Flood risk " + (oldRisk == null || oldRisk.isEmpty() ? "—" : oldRisk)
                                + " → " + newRisk));
            }
        }

        if (!Objects.equals(previousSystemAlert, alertLevel) && alertLevel != null) {
            events.add(new MonitoringEventDTO(time, mapAlertSeverity(alertLevel), null,
                    "SYSTEM ALERT: " + (previousSystemAlert == null ? "—" : previousSystemAlert)
                            + " → " + alertLevel));
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

        previousCepAlerts = current;
        return events;
    }

    private <T> Map<String, T> index(Class<T> type, Function<T, Location> locator) {
        Map<String, T> map = new HashMap<>();
        for (Object o : session.getObjects(obj -> type.isInstance(obj))) {
            T fact = type.cast(o);
            Location loc = locator.apply(fact);
            if (loc != null && loc.getCode() != null) {
                map.put(loc.getCode(), fact);
            }
        }
        return map;
    }

    private double nextValue(Sensor s) {
        String key = sensorKey(s);
        double cur = values.getOrDefault(key, baseline(s.getSensorType()));
        switch (s.getSensorType()) {
            case WATER_LEVEL:
                return meanRevert(cur, 180.0, 70.0, 30.0, 650.0);
            case FLOW_RATE:
                return meanRevert(cur, 1800.0, 600.0, 100.0, 4500.0);
            case PUMP_STATUS:
            default:
                double r = random.nextDouble();
                if (r < 0.82) {
                    return 1.0; 
                } else if (r < 0.93) {
                    return 0.0;
                }
                return -1.0;
        }
    }

    private double meanRevert(double cur, double baseline, double amplitude, double min, double max) {
        double drift = (baseline - cur) * 0.15;
        double noise = (random.nextDouble() - 0.5) * 2.0 * amplitude;
        double next = cur + drift + noise;
        next = Math.max(min, Math.min(max, next));
        return Math.round(next * 10.0) / 10.0;
    }

    private double baseline(SensorType type) {
        switch (type) {
            case WATER_LEVEL:
                return 180.0;
            case FLOW_RATE:
                return 1800.0;
            case PUMP_STATUS:
            default:
                return 1.0;
        }
    }

    private String severity(MonitoringLocationDTO d) {
        int s = 0;
        s = Math.max(s, riskScore(d.getRiskLevel()));
        s = Math.max(s, waterScore(d.getWaterLevel()));
        if ("OFFLINE".equals(d.getCapacityLevel())) {
            s = Math.max(s, 3);
        } else if ("MINIMAL".equals(d.getCapacityLevel())) {
            s = Math.max(s, 2);
        }
        return severityLabel(s);
    }

    private int riskScore(String risk) {
        if (risk == null) {
            return 0;
        }
        switch (risk) {
            case "EXTREME": return 3;
            case "HIGH":    return 2;
            case "MODERATE": return 1;
            default:        return 0;
        }
    }

    private int waterScore(String level) {
        if (level == null) {
            return 0;
        }
        switch (level) {
            case "CRITICAL": return 3;
            case "HIGH":     return 2;
            case "ELEVATED": return 1;
            default:         return 0;
        }
    }

    private String severityLabel(int score) {
        switch (score) {
            case 3:  return "CRITICAL";
            case 2:  return "DANGER";
            case 1:  return "WARNING";
            default: return "NORMAL";
        }
    }

    private String mapAlertSeverity(String alertLevel) {
        if (alertLevel == null) {
            return "NORMAL";
        }
        switch (alertLevel) {
            case "RED":    return "CRITICAL";
            case "ORANGE": return "DANGER";
            case "YELLOW": return "WARNING";
            default:       return "NORMAL";
        }
    }

    private String sensorKey(Sensor s) {
        return sensorKey(s.getLocation().getCode(), s.getTagName());
    }

    private String sensorKey(String locationCode, String tagName) {
        return locationCode + "|" + tagName;
    }
}
