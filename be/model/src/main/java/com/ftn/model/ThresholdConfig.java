package com.ftn.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "threshold_configs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"location_type", "parameter_type"}))
public class ThresholdConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false)
    private LocationType locationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "parameter_type", nullable = false)
    private ParameterType parameterType;

    @Column(name = "normal_max", nullable = false)
    private double normalMax;

    @Column(name = "warning_max", nullable = false)
    private double warningMax;

    @Column(name = "critical_max", nullable = false)
    private double criticalMax;

    public ThresholdConfig() {
    }

    public ThresholdConfig(LocationType locationType, ParameterType parameterType,
                           double normalMax, double warningMax, double criticalMax) {
        this.locationType = locationType;
        this.parameterType = parameterType;
        this.normalMax = normalMax;
        this.warningMax = warningMax;
        this.criticalMax = criticalMax;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocationType getLocationType() { return locationType; }
    public void setLocationType(LocationType locationType) { this.locationType = locationType; }

    public ParameterType getParameterType() { return parameterType; }
    public void setParameterType(ParameterType parameterType) { this.parameterType = parameterType; }

    public double getNormalMax() { return normalMax; }
    public void setNormalMax(double normalMax) { this.normalMax = normalMax; }

    public double getWarningMax() { return warningMax; }
    public void setWarningMax(double warningMax) { this.warningMax = warningMax; }

    public double getCriticalMax() { return criticalMax; }
    public void setCriticalMax(double criticalMax) { this.criticalMax = criticalMax; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThresholdConfig)) return false;
        ThresholdConfig other = (ThresholdConfig) o;
        return locationType == other.locationType && parameterType == other.parameterType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationType, parameterType);
    }

    @Override
    public String toString() {
        return "ThresholdConfig[" + locationType + "/" + parameterType
                + " n=" + normalMax + " w=" + warningMax + " c=" + criticalMax + "]";
    }
}
