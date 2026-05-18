package com.ftn.model;

import java.io.Serializable;

public class WeatherCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    private double precipitation;

    public WeatherCondition() {
    }

    public WeatherCondition(double precipitation) {
        this.precipitation = precipitation;
    }

    public double getPrecipitation() { return precipitation; }
    public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }

    @Override
    public String toString() {
        return "WeatherCondition{" +
                "precipitation=" + precipitation +
                '}';
    }
}
