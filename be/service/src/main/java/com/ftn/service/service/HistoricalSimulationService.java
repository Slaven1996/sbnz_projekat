package com.ftn.service.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ftn.model.FloodRiskAssessment;
import com.ftn.model.FlowRateStatus;
import com.ftn.model.InterventionRecommendation;
import com.ftn.model.Location;
import com.ftn.model.SensorReading;
import com.ftn.model.Sensor;
import com.ftn.model.StationCapacity;
import com.ftn.model.SystemAlert;
import com.ftn.model.ThresholdConfig;
import com.ftn.model.TrendData;
import com.ftn.model.WaterLevelStatus;
import com.ftn.model.WeatherCondition;
import com.ftn.model.enums.SensorType;
import com.ftn.service.dto.simulation.LocationStateDTO;
import com.ftn.service.dto.simulation.SimulationResultDTO;
import com.ftn.service.dto.simulation.SimulationStep;
import com.ftn.service.dto.simulation.TimelineEventDTO;
import com.ftn.service.exception.BadRequestException;
import com.ftn.service.repository.LocationRepository;
import com.ftn.service.repository.SensorRepository;
import com.ftn.service.repository.ThresholdConfigRepository;
import com.ftn.service.repository.TrendDataRepository;
import com.ftn.service.repository.WeatherConditionRepository;

@Service
@Transactional(readOnly = true)
public class HistoricalSimulationService {

    private final TrendDataRepository trendDataRepository;
    private final LocationRepository locationRepository;
    private final ThresholdConfigRepository thresholdConfigRepository;
    private final WeatherConditionRepository weatherConditionRepository;
    private final SensorRepository sensorRepository;
    private final RuleTemplateService ruleTemplateService;

    public HistoricalSimulationService(TrendDataRepository trendDataRepository,
                                       LocationRepository locationRepository,
                                       ThresholdConfigRepository thresholdConfigRepository,
                                       WeatherConditionRepository weatherConditionRepository,
                                       SensorRepository sensorRepository,
                                       RuleTemplateService ruleTemplateService) {
        this.trendDataRepository = trendDataRepository;
        this.locationRepository = locationRepository;
        this.thresholdConfigRepository = thresholdConfigRepository;
        this.weatherConditionRepository = weatherConditionRepository;
        this.sensorRepository = sensorRepository;
        this.ruleTemplateService = ruleTemplateService;
    }

    public SimulationResultDTO simulate(LocalDateTime startDate, LocalDateTime endDate,
                                        String locationCode, String stepUnit) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("startDate and endDate are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must be before or equal to endDate");
        }
        SimulationStep stepEnum = "DAY".equalsIgnoreCase(stepUnit) ? SimulationStep.DAY : SimulationStep.HOUR;
        ChronoUnit step = stepEnum == SimulationStep.DAY ? ChronoUnit.DAYS : ChronoUnit.HOURS;

        String location = (locationCode == null || locationCode.trim().isEmpty()) ? null : locationCode;
        List<TrendData> readings = trendDataRepository.searchAll(
                location, null, startDate, endDate,
                Sort.by(Sort.Direction.ASC, "logTime"));
        if (readings.isEmpty()) {
            throw new BadRequestException(
                    "No trend data found in the selected period - nothing to simulate.");
        }

        List<ThresholdConfig> thresholds = thresholdConfigRepository.findAll();
        KieBase kieBase = ruleTemplateService.build(thresholds);

        TreeSet<String> locationCodes = readings.stream()
                .map(TrendData::getLocationCode)
                .collect(Collectors.toCollection(TreeSet::new));
        Map<String, Location> locations = new LinkedHashMap<>();
        for (String code : locationCodes) {
            locationRepository.findByCode(code).ifPresent(loc -> locations.put(code, loc));
        }
        if (locations.isEmpty()) {
            throw new BadRequestException(
                    "Trend data references no known locations - cannot simulate.");
        }

        Map<String, SensorType> sensorTypes = new HashMap<>();
        for (Sensor s : sensorRepository.findAll()) {
            if (s.getLocation() != null) {
                sensorTypes.put(key(s.getLocation().getCode(), s.getTagName()), s.getSensorType());
            }
        }

        KieSession session = kieBase.newKieSession();
        Map<String, FactHandle> readingHandles = new HashMap<>();
        try {
            for (ThresholdConfig tc : thresholds) {
                session.insert(tc);
            }
            for (Location loc : locations.values()) {
                session.insert(loc);
                weatherConditionRepository.findByLocationId(loc.getId()).ifPresent(
                        wc -> session.insert(new WeatherCondition(loc, wc.getPrecipitation())));
            }

            Map<LocalDateTime, List<TrendData>> steps = groupByStep(readings, step);

            List<TimelineEventDTO> timeline = new ArrayList<>();
            Map<String, LocationStateDTO> previous = new HashMap<>();
            String previousAlert = null;

            for (Map.Entry<LocalDateTime, List<TrendData>> entry : steps.entrySet()) {
                TimelineEventDTO event = new TimelineEventDTO();
                event.setStepTime(entry.getKey());

                for (TrendData td : entry.getValue()) {
                    Location loc = locations.get(td.getLocationCode());
                    if (loc == null) {
                        continue;
                    }
                    SensorType type = sensorTypes.get(key(td.getLocationCode(), td.getTagName()));
                    if (type == null) {
                        continue;
                    }
                    applyReading(session, readingHandles, loc, type, td);
                    event.getAppliedReadings().add(String.format("%s / %s (%s) = %s",
                            td.getLocationCode(), td.getTagName(), type, trim(td.getTagValue())));
                }

                int fired = session.fireAllRules();
                event.setFiredRules(fired);

                Map<String, LocationStateDTO> current = snapshot(session, locations);
                event.setLocationStates(new ArrayList<>(current.values()));

                SystemAlert alert = null;
                for (Object o : session.getObjects(obj -> obj instanceof SystemAlert)) {
                    alert = (SystemAlert) o;
                }
                String alertLevel = alert != null ? alert.getLevel().name() : null;
                event.setSystemAlertLevel(alertLevel);
                event.setSystemAlertDescription(alert != null ? alert.getDescription() : null);

                event.setChanges(diff(previous, current, previousAlert, alertLevel));

                timeline.add(event);
                previous = current;
                previousAlert = alertLevel;
            }

            SimulationResultDTO result = new SimulationResultDTO();
            result.setStartDate(startDate);
            result.setEndDate(endDate);
            result.setStepUnit(stepEnum);
            result.setTotalReadings(readings.size());
            result.setStepCount(timeline.size());
            result.setLocationsInvolved(new ArrayList<>(locations.keySet()));
            result.setTimeline(timeline);
            return result;
        } finally {
            session.dispose();
        }
    }

    private void applyReading(KieSession session, Map<String, FactHandle> handles,
                              Location loc, SensorType type, TrendData td) {
        String handleKey = key(td.getLocationCode(), td.getTagName());
        Date ts = toDate(td.getLogTime());
        FactHandle handle = handles.get(handleKey);
        if (handle == null) {
            SensorReading reading = new SensorReading(loc, type, td.getTagName(), td.getTagValue(), ts);
            handles.put(handleKey, session.insert(reading));
        } else {
            SensorReading reading = (SensorReading) session.getObject(handle);
            reading.setValue(td.getTagValue());
            reading.setTimestamp(ts);
            session.update(handle, reading);
        }
    }

    private Map<LocalDateTime, List<TrendData>> groupByStep(List<TrendData> readings, ChronoUnit step) {
        Map<LocalDateTime, List<TrendData>> grouped = new LinkedHashMap<>();
        for (TrendData td : readings) {
            LocalDateTime bucket = td.getLogTime().truncatedTo(step);
            grouped.computeIfAbsent(bucket, k -> new ArrayList<>()).add(td);
        }
        return grouped;
    }

    private Map<String, LocationStateDTO> snapshot(KieSession session, Map<String, Location> locations) {
        Map<String, WaterLevelStatus> wls = indexByLocation(session, WaterLevelStatus.class, WaterLevelStatus::getLocation);
        Map<String, FlowRateStatus> frs = indexByLocation(session, FlowRateStatus.class, FlowRateStatus::getLocation);
        Map<String, StationCapacity> caps = indexByLocation(session, StationCapacity.class, StationCapacity::getLocation);
        Map<String, FloodRiskAssessment> risks = indexByLocation(session, FloodRiskAssessment.class, FloodRiskAssessment::getLocation);
        Map<String, InterventionRecommendation> recs = indexByLocation(session, InterventionRecommendation.class, InterventionRecommendation::getLocation);

        Map<String, LocationStateDTO> result = new LinkedHashMap<>();
        for (Map.Entry<String, Location> e : locations.entrySet()) {
            String code = e.getKey();
            Location loc = e.getValue();
            LocationStateDTO dto = new LocationStateDTO();
            dto.setLocationCode(code);
            dto.setLocationType(loc.getType() != null ? loc.getType().name() : null);
            dto.setZoneCode(loc.getZone() != null ? loc.getZone().getCode() : null);

            WaterLevelStatus w = wls.get(code);
            if (w != null) {
                dto.setWaterLevel(w.getLevel().name());
                dto.setWaterValue(w.getValue());
            }
            FlowRateStatus f = frs.get(code);
            if (f != null) {
                dto.setFlowLevel(f.getLevel().name());
                dto.setFlowValue(f.getValue());
            }
            StationCapacity c = caps.get(code);
            if (c != null) {
                dto.setCapacityLevel(c.getLevel().name());
                dto.setActivePumps(c.getActivePumps());
                dto.setTotalPumps(c.getTotalPumps());
            }
            FloodRiskAssessment r = risks.get(code);
            if (r != null) {
                dto.setRiskLevel(r.getRiskLevel().name());
                dto.setRiskReason(r.getReason());
            }
            InterventionRecommendation rec = recs.get(code);
            if (rec != null) {
                dto.setRecommendation(rec.getType().name());
                dto.setRecommendationPriority(rec.getPriority().name());
                dto.setRecommendationDescription(rec.getDescription());
            }
            result.put(code, dto);
        }
        return result;
    }

    private List<String> diff(Map<String, LocationStateDTO> prev, Map<String, LocationStateDTO> cur,
                              String prevAlert, String curAlert) {
        List<String> changes = new ArrayList<>();
        for (Map.Entry<String, LocationStateDTO> e : cur.entrySet()) {
            String code = e.getKey();
            LocationStateDTO c = e.getValue();
            LocationStateDTO p = prev.get(code);
            changes.addAll(transition(code, "water level", p == null ? null : p.getWaterLevel(), c.getWaterLevel()));
            changes.addAll(transition(code, "flow rate", p == null ? null : p.getFlowLevel(), c.getFlowLevel()));
            changes.addAll(transition(code, "pump capacity", p == null ? null : p.getCapacityLevel(), c.getCapacityLevel()));
            changes.addAll(transition(code, "flood risk", p == null ? null : p.getRiskLevel(), c.getRiskLevel()));
            changes.addAll(transition(code, "recommendation", p == null ? null : p.getRecommendation(), c.getRecommendation()));
        }
        if (!Objects.equals(prevAlert, curAlert) && curAlert != null) {
            changes.add(String.format("SYSTEM ALERT: %s → %s",
                    prevAlert == null ? "—" : prevAlert, curAlert));
        }
        return changes;
    }

    private List<String> transition(String code, String label, String from, String to) {
        if (Objects.equals(from, to)) {
            return Collections.emptyList();
        }
        if (from == null) {
            return Collections.singletonList(
                    String.format("%s: %s → %s", code, label, to));
        }
        if (to == null) {
            return Collections.singletonList(
                    String.format("%s: %s %s → none", code, label, from));
        }
        return Collections.singletonList(
                String.format("%s: %s %s → %s", code, label, from, to));
    }

    private <T> Map<String, T> indexByLocation(KieSession session, Class<T> type,
                                               Function<T, Location> locator) {
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

    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String key(String locationCode, String tagName) {
        return locationCode + "|" + tagName;
    }

    private String trim(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v)) {
            return String.valueOf((long) v);
        }
        return String.valueOf(v);
    }
}
