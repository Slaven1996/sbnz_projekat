package com.ftn.service.utils;

public class SensorValueRange {

    private double baseline;
    private double amplitude;
    private double min;
    private double max;
    private double step;

    public SensorValueRange() {
    }

    public SensorValueRange(double baseline, double amplitude, double min, double max, double step) {
        this.baseline = baseline;
        this.amplitude = amplitude;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getBaseline() { return baseline; }
    public void setBaseline(double baseline) { this.baseline = baseline; }

    public double getAmplitude() { return amplitude; }
    public void setAmplitude(double amplitude) { this.amplitude = amplitude; }

    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; }

    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }

    public double getStep() { return step; }
    public void setStep(double step) { this.step = step; }
}
