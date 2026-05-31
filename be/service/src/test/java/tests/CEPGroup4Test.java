package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.drools.core.ClockType;
import org.drools.core.time.SessionPseudoClock;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.io.ResourceFactory;

import com.ftn.model.Location;
import com.ftn.model.PumpOperationalStatus;
import com.ftn.model.enums.LocationType;
import com.ftn.model.enums.PumpEventType;
import com.ftn.model.enums.PumpState;
import com.ftn.model.enums.SensorType;
import com.ftn.model.events.ConnectionLostAlert;
import com.ftn.model.events.HeartbeatEvent;
import com.ftn.model.events.PumpConnectionLostAlert;
import com.ftn.model.events.PumpEvent;
import com.ftn.model.events.PumpFailureAlert;
import com.ftn.model.events.RapidWaterLevelRise;
import com.ftn.model.events.SensorReadingEvent;


public class CEPGroup4Test {

    private static final String DRL_PATH =
            "../kjar/src/main/resources/rules/group4-cep.drl";

    private KieSession newCepSession() {
        File drl = new File(DRL_PATH).getAbsoluteFile();
        if (!drl.exists()) {
            throw new IllegalStateException(
                    "Ne mogu naci drl fajl: " + drl.getAbsolutePath());
        }

        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write("src/main/resources/rules/group4-cep.drl",
                ResourceFactory.newFileResource(drl));

        KieBuilder kbuilder = ks.newKieBuilder(kfs);
        kbuilder.buildAll();
        if (kbuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException(
                    "Greska u prevodjenju group4-cep.drl: " + kbuilder.getResults());
        }

        KieContainer kContainer = ks.newKieContainer(kbuilder.getKieModule().getReleaseId());
        KieBaseConfiguration kbconf = ks.newKieBaseConfiguration();
        kbconf.setOption(EventProcessingOption.STREAM);
        KieBase kbase = kContainer.newKieBase(kbconf);

        KieSessionConfiguration ksconf = ks.newKieSessionConfiguration();
        ksconf.setOption(ClockTypeOption.get(ClockType.PSEUDO_CLOCK.getId()));
        return kbase.newKieSession(ksconf, null);
    }

    private static <T> int count(KieSession ks, Class<T> clazz) {
        Collection<?> facts = ks.getObjects(new ClassObjectFilter(clazz));
        return facts.size();
    }

    // ---------------------------------------------------------------------
    // Scenario 1: Nagli porast vodostaja
    // Dva WATER_LEVEL ocitavanja na istoj lokaciji u prozoru od 30 min,
    // drugo ocitavanje je 60 cm vise od prvog (prag je 50 cm).
    // Ocekivano: tacno jedan RapidWaterLevelRise dogadjaj.
    // ---------------------------------------------------------------------
    @Test
    public void testRapidWaterLevelRise() {
        KieSession ksession = newCepSession();
        try {
            SessionPseudoClock clock = ksession.getSessionClock();
            String loc = "LOC_RIVER";

            ksession.insert(new SensorReadingEvent(
                    loc, SensorType.WATER_LEVEL, "WL", 100.0,
                    new Date(clock.getCurrentTime())));
            ksession.fireAllRules();

            clock.advanceTime(10, TimeUnit.MINUTES);
            ksession.insert(new SensorReadingEvent(
                    loc, SensorType.WATER_LEVEL, "WL", 160.0,
                    new Date(clock.getCurrentTime())));
            ksession.fireAllRules();
            
            System.out.println();
            System.out.println("RapidWaterLevelRise events: " + count(ksession, RapidWaterLevelRise.class));
            System.out.println();

            assertEquals(1, count(ksession, RapidWaterLevelRise.class));
        } finally {
            ksession.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // Scenario 2: Obrazac kvara pumpe (RESTART pattern)
    // Cetiri RESTART dogadjaja iste pumpe u roku od 1h ($p1 + 3 nakon $p1).
    // Ocekivano: tacno jedan PumpFailureAlert
    // ---------------------------------------------------------------------
    @Test
    public void testPumpRestartPatternFiresAlert() {
        KieSession ksession = newCepSession();
        try {
            SessionPseudoClock clock = ksession.getSessionClock();
            String loc = "LOC_PUMP";
            String pumpId = "P1";

            for (int i = 0; i < 4; i++) {
                ksession.insert(new PumpEvent(
                        loc, pumpId, PumpEventType.RESTART,
                        new Date(clock.getCurrentTime())));
                clock.advanceTime(10, TimeUnit.MINUTES);
                ksession.fireAllRules();
            }

            System.out.println();
            System.out.println("PumpFailureAlert events: " + count(ksession, PumpFailureAlert.class));
            System.out.println();

            assertEquals(1, count(ksession, PumpFailureAlert.class));
        } finally {
            ksession.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // Scenario 3: Gubitak komunikacije + obnavljanje
    // - Saljemo heartbeats svakih 1 min (5 puta): bez alarma.
    // - Pomjeramo sat 6 min unaprijed bez heartbeats-a:
    //     -> jedan ConnectionLostAlert.
    // - Jedan heartbeat nakon alarma: alarm se povlaci.
    // ---------------------------------------------------------------------
    @Test
    public void testConnectionLostAndRecovery() {
        KieSession ksession = newCepSession();
        try {
            SessionPseudoClock clock = ksession.getSessionClock();
            Location loc = new Location("LOC_HB", LocationType.RIVER);
            ksession.insert(loc);

            for (int i = 0; i < 5; i++) {
                ksession.insert(new HeartbeatEvent("LOC_HB",
                        new Date(clock.getCurrentTime())));
                clock.advanceTime(1, TimeUnit.MINUTES);
                ksession.fireAllRules();
            }

            System.out.println();
            System.out.println("ConnectionLostAlert events: " + count(ksession, ConnectionLostAlert.class));

            assertEquals(0, count(ksession, ConnectionLostAlert.class));

            clock.advanceTime(6, TimeUnit.MINUTES);
            ksession.fireAllRules();

            System.out.println();
            System.out.println("ConnectionLostAlert events: " + count(ksession, ConnectionLostAlert.class));
            System.out.println();

            assertEquals(1, count(ksession, ConnectionLostAlert.class));

            clock.advanceTime(1, TimeUnit.SECONDS);
            ksession.insert(new HeartbeatEvent("LOC_HB",
                    new Date(clock.getCurrentTime())));
            ksession.fireAllRules();

            System.out.println("ConnectionLostAlert events: " + count(ksession, ConnectionLostAlert.class));
            System.out.println();

            assertEquals(0, count(ksession, ConnectionLostAlert.class));
        } finally {
            ksession.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // Scenario 4: Gubitak komunikacije sa pumpom + obnavljanje
    // - PumpOperationalStatus + redovna PUMP_STATUS ocitavanja u prozoru
    //   od 10 min: bez alarma.
    // - Pauza od 11 min bez ocitavanja -> jedan PumpConnectionLostAlert.
    // - Novo PUMP_STATUS ocitavanje nakon alarma -> alarm se povlaci.
    // ---------------------------------------------------------------------
    @Test
    public void testPumpConnectionLostAndRecovery() {
        KieSession ksession = newCepSession();
        try {
            SessionPseudoClock clock = ksession.getSessionClock();
            Location loc = new Location("LOC_PUMPST", LocationType.PUMP_STATION);
            String pumpId = "PUMP_A";

            PumpOperationalStatus ps = new PumpOperationalStatus(
                    loc, pumpId, PumpState.ACTIVE,
                    new Date(clock.getCurrentTime()));
            ksession.insert(ps);

            for (int i = 0; i < 4; i++) {
                ksession.insert(new SensorReadingEvent(
                        "LOC_PUMPST", SensorType.PUMP_STATUS, pumpId, 1.0,
                        new Date(clock.getCurrentTime())));
                clock.advanceTime(2, TimeUnit.MINUTES);
                ksession.fireAllRules();
            }

            System.out.println();
            System.out.println("PumpConnectionLostAlert events: " + count(ksession, PumpConnectionLostAlert.class));

            assertEquals(0, count(ksession, PumpConnectionLostAlert.class));

            clock.advanceTime(11, TimeUnit.MINUTES);
            ksession.fireAllRules();

            System.out.println();
            System.out.println("PumpConnectionLostAlert events: " + count(ksession, PumpConnectionLostAlert.class));

            assertEquals(1, count(ksession, PumpConnectionLostAlert.class));

            clock.advanceTime(1, TimeUnit.SECONDS);
            ksession.insert(new SensorReadingEvent(
                    "LOC_PUMPST", SensorType.PUMP_STATUS, pumpId, 1.0,
                    new Date(clock.getCurrentTime())));
            ksession.fireAllRules();

            System.out.println();
            System.out.println("PumpConnectionLostAlert events: " + count(ksession, PumpConnectionLostAlert.class));

            assertEquals(0, count(ksession, PumpConnectionLostAlert.class));
        } finally {
            ksession.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // Scenario 5: Pumpa u stanju FAULTY + oporavak
    // - PumpOperationalStatus(state = FAULTY) -> jedan PumpFailureAlert.
    // - Novi PumpOperationalStatus(state = ACTIVE) sa kasnijim timestamp-om -> alarm se povlaci.
    // ---------------------------------------------------------------------
    @Test
    public void testFaultyPumpAlertAndRecovery() {
        KieSession ksession = newCepSession();
        try {
            Location loc = new Location("LOC_FAULT", LocationType.PUMP_STATION);
            String pumpId = "PUMP_F";

            long now = System.currentTimeMillis();
            PumpOperationalStatus faulty = new PumpOperationalStatus(
                    loc, pumpId, PumpState.FAULTY, new Date(now));
            ksession.insert(faulty);
            ksession.fireAllRules();

            System.out.println();
            System.out.println("PumpFailureAlert events: " + count(ksession, PumpFailureAlert.class));

            assertEquals(1, count(ksession, PumpFailureAlert.class));

            PumpOperationalStatus active = new PumpOperationalStatus(
                    loc, pumpId, PumpState.ACTIVE,
                    new Date(now + TimeUnit.HOURS.toMillis(1)));
            ksession.insert(active);
            ksession.fireAllRules();


            System.out.println();
            System.out.println("PumpFailureAlert events: " + count(ksession, PumpFailureAlert.class));

            assertEquals(0, count(ksession, PumpFailureAlert.class));
        } finally {
            ksession.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // Scenario 6: Izolacija po lokaciji - dva nezavisna porasta vodostaja
    // Dvije razlicite lokacije, svaka sa svojim parom WATER_LEVEL
    // ocitavanja koji zadovoljava pravilo.
    // ---------------------------------------------------------------------
    @Test
    public void testRapidRiseIsolatedPerLocation() {
        KieSession ksession = newCepSession();
        try {
            SessionPseudoClock clock = ksession.getSessionClock();

            ksession.insert(new SensorReadingEvent(
                    "LOC_A", SensorType.WATER_LEVEL, "WL_A", 100.0,
                    new Date(clock.getCurrentTime())));
            ksession.insert(new SensorReadingEvent(
                    "LOC_B", SensorType.WATER_LEVEL, "WL_B", 200.0,
                    new Date(clock.getCurrentTime())));
            ksession.fireAllRules();

            clock.advanceTime(5, TimeUnit.MINUTES);
            ksession.insert(new SensorReadingEvent(
                    "LOC_A", SensorType.WATER_LEVEL, "WL_A", 170.0,
                    new Date(clock.getCurrentTime())));
            ksession.insert(new SensorReadingEvent(
                    "LOC_B", SensorType.WATER_LEVEL, "WL_B", 260.0,
                    new Date(clock.getCurrentTime())));
            ksession.fireAllRules();

            System.out.println();
            System.out.println("RapidWaterLevelRise events: " + count(ksession, RapidWaterLevelRise.class));
            System.out.println();

            assertEquals(2, count(ksession, RapidWaterLevelRise.class));
        } finally {
            ksession.dispose();
        }
    }
}
