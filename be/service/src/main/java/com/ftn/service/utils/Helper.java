package com.ftn.service.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

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
import com.ftn.service.dto.monitoring.MonitoringLocationDTO;
import com.ftn.service.dto.simulation.LocationStateDTO;
import com.ftn.service.repository.WeatherConditionRepository;

public class Helper {

    public static String sensorKey(String locationCode, String tagName) {
        return locationCode + "|" + tagName;
    }

    public static String sensorKey(Sensor s) {
        return sensorKey(s.getLocation().getCode(), s.getTagName());
    }

    public static Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String trim(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v)) {
            return String.valueOf((long) v);
        }
        return String.valueOf(v);
    }

    public static Map<LocalDateTime, List<TrendData>> groupByStep(List<TrendData> readings, ChronoUnit step) {
        Map<LocalDateTime, List<TrendData>> grouped = new LinkedHashMap<>();
        for (TrendData td : readings) {
            LocalDateTime bucket = td.getLogTime().truncatedTo(step);
            grouped.computeIfAbsent(bucket, k -> new ArrayList<>()).add(td);
        }
        return grouped;
    }

    public static void seedFacts(KieSession session, List<ThresholdConfig> thresholds,
                                 Collection<Location> locations, WeatherConditionRepository weatherRepo) {
        for (ThresholdConfig tc : thresholds) {
            session.insert(tc);
        }
        for (Location loc : locations) {
            session.insert(loc);
            weatherRepo.findByLocationId(loc.getId()).ifPresent(
                    wc -> session.insert(new WeatherCondition(loc, wc.getPrecipitation())));
        }
    }

    public static FactHandle applyReading(KieSession session, Map<String, FactHandle> handles,
                                          Location loc, SensorType type, String tagName,
                                          double value, Date ts) {
        String handleKey = sensorKey(loc.getCode(), tagName);
        FactHandle handle = handles.get(handleKey);
        if (handle == null) {
            SensorReading reading = new SensorReading(loc, type, tagName, value, ts);
            FactHandle inserted = session.insert(reading);
            handles.put(handleKey, inserted);
            return inserted;
        }
        SensorReading reading = (SensorReading) session.getObject(handle);
        reading.setValue(value);
        reading.setTimestamp(ts);
        session.update(handle, reading);
        return handle;
    }

    public static <T> Map<String, T> index(KieSession session, Class<T> type, Function<T, Location> locator) {
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

    public static SystemAlert latestSystemAlert(KieSession session) {
        return session.getObjects(obj -> obj instanceof SystemAlert).stream()
                .map(SystemAlert.class::cast)
                .findFirst()
                .orElse(null);
    }

    public static Map<String, LocationStateDTO> snapshot(KieSession session, Map<String, Location> locations) {
        Map<String, WaterLevelStatus> wls = index(session, WaterLevelStatus.class, WaterLevelStatus::getLocation);
        Map<String, FlowRateStatus> frs = index(session, FlowRateStatus.class, FlowRateStatus::getLocation);
        Map<String, StationCapacity> caps = index(session, StationCapacity.class, StationCapacity::getLocation);
        Map<String, FloodRiskAssessment> risks = index(session, FloodRiskAssessment.class, FloodRiskAssessment::getLocation);
        Map<String, InterventionRecommendation> recs = index(session, InterventionRecommendation.class, InterventionRecommendation::getLocation);

        Map<String, LocationStateDTO> result = new LinkedHashMap<>();
        for (Map.Entry<String, Location> e : locations.entrySet()) {
            String code = e.getKey();
            Location loc = e.getValue();
            LocationStateDTO dto = new LocationStateDTO();
            dto.setLocationCode(code);
            dto.setLocationType(loc.getType());
            dto.setZoneCode(loc.getZone() != null ? loc.getZone().getCode() : null);

            WaterLevelStatus w = wls.get(code);
            if (w != null) {
                dto.setWaterLevel(w.getLevel());
                dto.setWaterValue(w.getValue());
            }
            FlowRateStatus f = frs.get(code);
            if (f != null) {
                dto.setFlowLevel(f.getLevel());
                dto.setFlowValue(f.getValue());
            }
            StationCapacity c = caps.get(code);
            if (c != null) {
                dto.setCapacityLevel(c.getLevel());
                dto.setActivePumps(c.getActivePumps());
                dto.setTotalPumps(c.getTotalPumps());
            }
            FloodRiskAssessment r = risks.get(code);
            if (r != null) {
                dto.setRiskLevel(r.getRiskLevel());
                dto.setRiskReason(r.getReason());
            }
            InterventionRecommendation rec = recs.get(code);
            if (rec != null) {
                dto.setRecommendation(rec.getType());
                dto.setRecommendationPriority(rec.getPriority());
                dto.setRecommendationDescription(rec.getDescription());
            }
            result.put(code, dto);
        }
        return result;
    }

    public static List<String> diff(Map<String, LocationStateDTO> prev, Map<String, LocationStateDTO> cur,
                                    String prevAlert, String curAlert) {
        List<String> changes = new ArrayList<>();
        for (Map.Entry<String, LocationStateDTO> e : cur.entrySet()) {
            String code = e.getKey();
            LocationStateDTO c = e.getValue();
            LocationStateDTO p = prev.get(code);
            transition(changes, code, "water level", p == null ? null : p.getWaterLevel(), c.getWaterLevel());
            transition(changes, code, "flow rate", p == null ? null : p.getFlowLevel(), c.getFlowLevel());
            transition(changes, code, "pump capacity", p == null ? null : p.getCapacityLevel(), c.getCapacityLevel());
            transition(changes, code, "flood risk", p == null ? null : p.getRiskLevel(), c.getRiskLevel());
            transition(changes, code, "recommendation", p == null ? null : p.getRecommendation(), c.getRecommendation());
        }
        if (!Objects.equals(prevAlert, curAlert) && curAlert != null) {
            changes.add(String.format("SYSTEM ALERT: %s -> %s",
                    prevAlert == null ? "-" : prevAlert, curAlert));
        }
        return changes;
    }

    public static void transition(List<String> changes, String code, String label, Object from, Object to) {
        if (Objects.equals(from, to)) {
            return;
        }
        if (from == null) {
            changes.add(String.format("%s: %s -> %s", code, label, to));
        } else if (to == null) {
            changes.add(String.format("%s: %s %s -> none", code, label, from));
        } else {
            changes.add(String.format("%s: %s %s -> %s", code, label, from, to));
        }
    }

    public static double baseline(SensorType type) {
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

    public static double meanRevert(Random random, double cur, double baseline, double amplitude,
                                    double min, double max) {
        double drift = (baseline - cur) * 0.15;
        double noise = (random.nextDouble() - 0.5) * 2.0 * amplitude;
        double next = cur + drift + noise;
        next = Math.max(min, Math.min(max, next));
        return Math.round(next * 10.0) / 10.0;
    }

    public static String severity(MonitoringLocationDTO d) {
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

    public static int riskScore(String risk) {
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

    public static int waterScore(String level) {
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

    public static String severityLabel(int score) {
        switch (score) {
            case 3:  return "CRITICAL";
            case 2:  return "DANGER";
            case 1:  return "WARNING";
            default: return "NORMAL";
        }
    }

    public static String mapAlertSeverity(String alertLevel) {
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
}
