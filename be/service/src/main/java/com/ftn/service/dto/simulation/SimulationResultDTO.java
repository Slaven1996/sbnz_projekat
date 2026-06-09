package com.ftn.service.dto.simulation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SimulationResultDTO {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SimulationStep stepUnit;
    private int totalReadings;
    private int stepCount;
    private List<String> locationsInvolved = new ArrayList<>();
    private List<TimelineEventDTO> timeline = new ArrayList<>();

    public SimulationResultDTO() {
    }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public SimulationStep getStepUnit() { return stepUnit; }
    public void setStepUnit(SimulationStep stepUnit) { this.stepUnit = stepUnit; }

    public int getTotalReadings() { return totalReadings; }
    public void setTotalReadings(int totalReadings) { this.totalReadings = totalReadings; }

    public int getStepCount() { return stepCount; }
    public void setStepCount(int stepCount) { this.stepCount = stepCount; }

    public List<String> getLocationsInvolved() { return locationsInvolved; }
    public void setLocationsInvolved(List<String> locationsInvolved) { this.locationsInvolved = locationsInvolved; }

    public List<TimelineEventDTO> getTimeline() { return timeline; }
    public void setTimeline(List<TimelineEventDTO> timeline) { this.timeline = timeline; }
}
