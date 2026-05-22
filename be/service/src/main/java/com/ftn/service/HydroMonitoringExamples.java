package com.ftn.service;

import java.util.Collection;
import java.util.Date;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.ftn.model.Location;
import com.ftn.model.ThresholdConfig;
import com.ftn.model.Zone;
import com.ftn.model.enums.LocationType;
import com.ftn.model.enums.ParameterType;
import com.ftn.model.enums.SensorType;
import com.ftn.model.FloodRiskAssessment;
import com.ftn.model.FlowRateStatus;
import com.ftn.model.InterventionRecommendation;
import com.ftn.model.PumpOperationalStatus;
import com.ftn.model.SensorReading;
import com.ftn.model.StationCapacity;
import com.ftn.model.SystemAlert;
import com.ftn.model.WaterLevelStatus;
import com.ftn.model.WeatherCondition;

public class HydroMonitoringExamples {

    public static void main(String[] args) {

        System.out.println();

        // Scenario 1: Sve normalno.
        // Jedna RIVER lokacija, nivo 120 cm (< normalMax 200), protok 1500
        // (< normalMax 2000), padavine 5 mm/h. Ocekivano:
        //   - WaterLevelStatus=NORMAL, FlowRateStatus=NORMAL
        //   - FloodRiskAssessment=LOW
        //   - SystemAlert=GREEN
        scenarioAllNormal();

        // Scenario 2: Normalan nivo + jake padavine = MODERATE rizik.
        // Pokazuje da meteo kontekst sam podize rizik iznad LOW iako su
        // sva ocitavanja jos uvijek u normalnom opsegu (pravilo 2.3).
        // scenarioRainOnly();

        // Scenario 3: Eskalacija u tri koraka na istoj lokaciji.
        // Demonstrira pravila ažuriranja iz Grupe 1 i eskalaciju rizika
        // iz Grupe 2 (LOW -> MODERATE -> HIGH -> EXTREME) kroz uzastopna
        // ocitavanja istog senzora sa rastucim vrijednostima.
        // Ocekivano nakon svakog ocitavanja vidimo:
        //   180 cm -> NORMAL/LOW
        //   300 cm -> ELEVATED/MODERATE
        //   400 cm -> HIGH/HIGH
        //   600 cm -> CRITICAL/EXTREME
        // scenarioEscalationOverTime();

        // Scenario 4: Hitna evakuacija - EXTREME rizik + OFFLINE pumpe.
        // Pumpna stanica sa kriticnim nivoom vode (400 cm > critMax 350)
        // i svih 5 pumpi u kvaru. Ocekivano:
        //   - WaterLevelStatus=CRITICAL, FloodRiskAssessment=EXTREME
        //   - StationCapacity=OFFLINE
        //   - InterventionRecommendation=EVACUATE (Priority.CRITICAL)
        //   - SystemAlert=ORANGE (jedna ACTIVATE/EVACUATE lokacija)
        // scenarioEvacuation();

        // Scenario 5: Crveni alarm - tri lokacije u vanrednom rezimu.
        // Tri lokacije sa EXTREME rizikom (svaka generise ACTIVATE/EVACUATE
        // preporuku). Ocekivano: SystemAlert eskaliran na RED.
        // scenarioRedAlert();

        // Scenario 6: Kompletan tok rezonovanja kroz sve tri grupe pravila.
        // Primjer sa dvije lokacije, LOC_RIJEKA
        // (RIVER) sa nivoom 380 cm + protokom 3500 m3/s, LOC_PUMPA
        // (PUMP_STATION) sa nivoom 200 cm i pet pumpi (3 ACTIVE, 1 FAULTY,
        // 1 IDLE). Ocekivani izlaz:
        //   - LOC_RIJEKA: WaterLevelStatus=HIGH, FlowRateStatus=HIGH,
        //                 FloodRiskAssessment=EXTREME, preporuka=ACTIVATE
        //   - LOC_PUMPA:  WaterLevelStatus=ELEVATED,
        //                 StationCapacity=REDUCED (3/5),
        //                 FloodRiskAssessment=MODERATE, preporuka=MONITOR
        //   - Globalni SystemAlert=ORANGE (1 ACTIVATE preporuka u sistemu)
        // scenarioFlowToOrangeAlert();

        System.out.println("=========================================================");
        System.out.println();
    }

    // Scenario 1 - Sve normalno
    private static void scenarioAllNormal() {
        printHeader("Scenario 1: Sve normalno");
        KieSession kSession = openSession();

        try {
            Location river = new Location("LOC_NORMAL", LocationType.RIVER);
            kSession.insert(river);
            insertDefaultThresholds(kSession);
            kSession.insert(new WeatherCondition(river, 5.0));
            kSession.insert(new SensorReading(river, SensorType.WATER_LEVEL, "WL", 120.0));
            kSession.insert(new SensorReading(river, SensorType.FLOW_RATE, "FR", 1500.0));

            int fired = kSession.fireAllRules();
            System.out.println("Pravila ispaljena: " + fired);
            dumpWorkingMemory(kSession);
        } finally {
            kSession.dispose();
        }
    }

    // Scenario 2 - Jaka kisa podize rizik iako su senzori normalni
    private static void scenarioRainOnly() {
        printHeader("Scenario 2: Normalan nivo + jake padavine = MODERATE");
        KieSession kSession = openSession();

        try {
            Location river = new Location("LOC_RAIN", LocationType.RIVER);
            kSession.insert(river);
            insertDefaultThresholds(kSession);
            kSession.insert(new WeatherCondition(river, 45.0));
            kSession.insert(new SensorReading(river, SensorType.WATER_LEVEL, "WL", 150.0));
            kSession.insert(new SensorReading(river, SensorType.FLOW_RATE, "FR", 1500.0));

            int fired = kSession.fireAllRules();
            System.out.println("Pravila ispaljena: " + fired);
            dumpWorkingMemory(kSession);
        } finally {
            kSession.dispose();
        }
    }

    // Scenario 3 - Eskalacija tokom vremena
    private static void scenarioEscalationOverTime() {
        printHeader("Scenario 3: Eskalacija tokom vremena");
        KieSession kSession = openSession();

        try {
            Location river = new Location("LOC_ESC", LocationType.RIVER);
            kSession.insert(river);
            insertDefaultThresholds(kSession);
            kSession.insert(new WeatherCondition(river, 2.0));

            double[] readings = {180.0, 300.0, 400.0, 600.0};
            for (int i = 0; i < readings.length; i++) {
                Date t = new Date(System.currentTimeMillis() + (i * 60_000L));
                kSession.insert(new SensorReading(river, SensorType.WATER_LEVEL, "WL", readings[i], t));
                int fired = kSession.fireAllRules();
                System.out.println("Korak " + (i + 1) + " (val=" + readings[i] + ") - pravila: " + fired);
                printFacts(kSession, WaterLevelStatus.class);
                printFacts(kSession, FloodRiskAssessment.class);
                printFacts(kSession, InterventionRecommendation.class);
                printFacts(kSession, SystemAlert.class);

            }
            System.out.println();
            System.out.println("Finalno stanje nakon svih ocitavanja:");
            dumpWorkingMemory(kSession);
        } finally {
            kSession.dispose();
        }
    }

    // Scenario 4 - Evakuacija: EXTREME + OFFLINE
    private static void scenarioEvacuation() {
        printHeader("Scenario 4: Evakuacija (EXTREME + OFFLINE)");
        KieSession kSession = openSession();

        try {
            Location pump = new Location("LOC_PUMP_DOWN", LocationType.PUMP_STATION);
            kSession.insert(pump);
            insertDefaultThresholds(kSession);
            kSession.insert(new WeatherCondition(pump, 60.0));
            kSession.insert(new SensorReading(pump, SensorType.WATER_LEVEL, "WL", 400.0));
            for (int i = 1; i <= 5; i++) {
                kSession.insert(new SensorReading(pump, SensorType.PUMP_STATUS, "P" + i, -1.0));
            }
            int fired = kSession.fireAllRules();
            System.out.println("Pravila ispaljena: " + fired);
            dumpWorkingMemory(kSession);
        } finally {
            kSession.dispose();
        }
    }

    // Scenario 5 - crveni alarm: 3 stanice sa ekstremnim rizikom
    private static void scenarioRedAlert() {
        printHeader("Scenario 5: crveni alarm - u 3 lokacije aktiviran vanredni rezim");
        KieSession kSession = openSession();

        try {
            Location l1 = new Location("LOC_R1", LocationType.RIVER);
            Location l2 = new Location("LOC_R2", LocationType.RIVER);
            Location l3 = new Location("LOC_R3", LocationType.RIVER);
            kSession.insert(l1);
            kSession.insert(l2);
            kSession.insert(l3);

            insertDefaultThresholds(kSession);
            kSession.insert(new WeatherCondition(l1, 30.0));
            kSession.insert(new WeatherCondition(l2, 30.0));
            kSession.insert(new WeatherCondition(l3, 30.0));

            kSession.insert(new SensorReading(l1, SensorType.WATER_LEVEL, "WL1", 550.0));
            kSession.insert(new SensorReading(l2, SensorType.WATER_LEVEL, "WL2", 600.0));
            kSession.insert(new SensorReading(l3, SensorType.WATER_LEVEL, "WL3", 700.0));

            int fired = kSession.fireAllRules();
            System.out.println("Pravila ispaljena: " + fired);
            dumpWorkingMemory(kSession);
        } finally {
            kSession.dispose();
        }
    }

    // Scenario 6 - Eskalacija toka do narandzastog alarma
    private static void scenarioFlowToOrangeAlert() {
        printHeader("Scenario 6: Eskalacija toka do narandzastog alarma");
        KieSession kSession = openSession();

        try {
            Zone north = new Zone("Z_NORTH", "Sjever");
            Zone south = new Zone("Z_SOUTH", "Jug");
            Location river = new Location("LOC_RIJEKA", LocationType.RIVER, north);
            Location pump = new Location("LOC_PUMPA", LocationType.PUMP_STATION, south);
            north.addLocation(river);
            south.addLocation(pump);

            kSession.insert(river);
            kSession.insert(pump);

            insertDefaultThresholds(kSession);

            kSession.insert(new WeatherCondition(river, 45.0));
            kSession.insert(new WeatherCondition(pump, 45.0));

            kSession.insert(new SensorReading(river, SensorType.WATER_LEVEL, "WL_R", 380.0));
            kSession.insert(new SensorReading(river, SensorType.FLOW_RATE, "FR_R", 3500.0));

            kSession.insert(new SensorReading(pump, SensorType.WATER_LEVEL, "WL_P", 200.0));
            kSession.insert(new SensorReading(pump, SensorType.PUMP_STATUS, "P1", 1.0));
            kSession.insert(new SensorReading(pump, SensorType.PUMP_STATUS, "P2", 1.0));
            kSession.insert(new SensorReading(pump, SensorType.PUMP_STATUS, "P3", 1.0));
            kSession.insert(new SensorReading(pump, SensorType.PUMP_STATUS, "P4", -1.0));
            kSession.insert(new SensorReading(pump, SensorType.PUMP_STATUS, "P5", 0.0));

            int fired = kSession.fireAllRules();
            System.out.println("Pravila ispaljena: " + fired);
            dumpWorkingMemory(kSession);
        } finally {
            kSession.dispose();
        }
    }

    private static KieSession openSession() {
        KieContainer kc = KieServices.Factory.get().getKieClasspathContainer();
        return kc.newKieSession("hydroKsession");
    }

    /**
     - RIVER:        WL n=200, w=350, c=500   |  FR n=2000, w=3000
     - CANAL:        WL n=100, w=180, c=250   |  FR n=500,  w=900
     - RESERVOIR:    WL n=500, w=700, c=850   |  FR n=100,  w=300
     - PUMP_STATION: WL n=150, w=250, c=350   |  FR n=100,  w=200
    */
    private static void insertDefaultThresholds(KieSession k) {
        k.insert(new ThresholdConfig(LocationType.RIVER,        ParameterType.WATER_LEVEL, 200, 350, 500));
        k.insert(new ThresholdConfig(LocationType.CANAL,        ParameterType.WATER_LEVEL, 100, 180, 250));
        k.insert(new ThresholdConfig(LocationType.RESERVOIR,    ParameterType.WATER_LEVEL, 500, 700, 850));
        k.insert(new ThresholdConfig(LocationType.PUMP_STATION, ParameterType.WATER_LEVEL, 150, 250, 350));
        
        k.insert(new ThresholdConfig(LocationType.RIVER,        ParameterType.FLOW_RATE,   2000, 3000));
        k.insert(new ThresholdConfig(LocationType.CANAL,        ParameterType.FLOW_RATE,    500,  900));
        k.insert(new ThresholdConfig(LocationType.RESERVOIR,    ParameterType.FLOW_RATE,    100,  300));
        k.insert(new ThresholdConfig(LocationType.PUMP_STATION, ParameterType.FLOW_RATE,    100,  200));
    }

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }

    private static void dumpWorkingMemory(KieSession k) {
        printFacts(k, WaterLevelStatus.class);
        printFacts(k, FlowRateStatus.class);
        printFacts(k, PumpOperationalStatus.class);
        printFacts(k, StationCapacity.class);
        printFacts(k, FloodRiskAssessment.class);
        printFacts(k, InterventionRecommendation.class);
        printFacts(k, SystemAlert.class);
    }

    private static <T> void printFacts(KieSession k, Class<T> clazz) {
        Collection<? extends Object> facts = k.getObjects(o -> clazz.isInstance(o));
        if (facts.isEmpty()) return;
        System.out.println("  " + clazz.getSimpleName() + " (" + facts.size() + "):");
        for (Object f : facts) {
            System.out.println("    - " + f);
        }
    }
}
