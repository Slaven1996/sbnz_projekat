package com.ftn.model;

import java.io.Serializable;

import com.ftn.model.enums.AlertLevel;

public class SystemAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    private AlertLevel level;
    private String description;

    public SystemAlert() {
    }

    public SystemAlert(AlertLevel level, String description) {
        this.level = level;
        this.description = description;
    }

    public AlertLevel getLevel() { return level; }
    public void setLevel(AlertLevel level) { this.level = level; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "SystemAlert[" + level + " - " + description + "]";
    }
}
