package com.ftn.service.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ftn.model.Location;
import com.ftn.model.Sensor;
import com.ftn.model.SystemAlert;
import com.ftn.model.ThresholdConfig;
import com.ftn.model.TrendData;
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
import com.ftn.service.utils.Helper;

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
                                        String stepUnit) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("startDate and endDate are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must be before or equal to endDate");
        }
        SimulationStep stepEnum = "DAY".equalsIgnoreCase(stepUnit) ? SimulationStep.DAY : SimulationStep.HOUR;
        ChronoUnit step = stepEnum == SimulationStep.DAY ? ChronoUnit.DAYS : ChronoUnit.HOURS;

        List<TrendData> readings = trendDataRepository.searchAll(
                null, null, startDate, endDate,
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
                sensorTypes.put(Helper.sensorKey(s.getLocation().getCode(), s.getTagName()),
                        s.getSensorType());
            }
        }

        KieSession session = kieBase.newKieSession();
        Map<String, FactHandle> readingHandles = new HashMap<>();
        try {
            Helper.seedFacts(session, thresholds, locations.values(), weatherConditionRepository);

            Map<LocalDateTime, List<TrendData>> steps = Helper.groupByStep(readings, step);

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
                    SensorType type = sensorTypes.get(
                            Helper.sensorKey(td.getLocationCode(), td.getTagName()));
                    if (type == null) {
                        continue;
                    }
                    Helper.applyReading(session, readingHandles, loc, type,
                            td.getTagName(), td.getTagValue(), Helper.toDate(td.getLogTime()));
                    event.getAppliedReadings().add(String.format("%s / %s (%s) = %s",
                            td.getLocationCode(), td.getTagName(), type, Helper.trim(td.getTagValue())));
                }

                int fired = session.fireAllRules();
                event.setFiredRules(fired);

                Map<String, LocationStateDTO> current = Helper.snapshot(session, locations);
                event.setLocationStates(new ArrayList<>(current.values()));

                SystemAlert alert = Helper.latestSystemAlert(session);
                String alertLevel = alert != null ? alert.getLevel().name() : null;
                event.setSystemAlertLevel(alertLevel);
                event.setSystemAlertDescription(alert != null ? alert.getDescription() : null);

                event.setChanges(Helper.diff(previous, current, previousAlert, alertLevel));

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

}
