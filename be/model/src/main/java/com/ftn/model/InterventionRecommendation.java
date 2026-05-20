package com.ftn.model;

import java.io.Serializable;

import com.ftn.model.enums.ActionType;
import com.ftn.model.enums.Priority;

public class InterventionRecommendation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Location location;
    private ActionType type;
    private Priority priority;
    private String description;

    public InterventionRecommendation() {
    }

    public InterventionRecommendation(Location location, ActionType type, Priority priority, String description) {
        this.location = location;
        this.type = type;
        this.priority = priority;
        this.description = description;
    }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public ActionType getType() { return type; }
    public void setType(ActionType type) { this.type = type; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "InterventionRecommendation[" + (location != null ? location.getCode() : "?")
                + " => " + type + " (" + priority + ") - " + description + "]";
    }
}
