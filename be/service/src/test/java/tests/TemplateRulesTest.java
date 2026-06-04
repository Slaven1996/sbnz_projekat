package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.drools.decisiontable.ExternalSpreadsheetCompiler;
import org.drools.template.DataProviderCompiler;
import org.drools.template.objects.ArrayDataProvider;
import org.junit.jupiter.api.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

import com.ftn.model.Location;
import com.ftn.model.SensorReading;
import com.ftn.model.WaterLevelStatus;
import com.ftn.model.enums.LocationType;
import com.ftn.model.enums.SensorType;
import com.ftn.model.enums.StatusLevel;

public class TemplateRulesTest {

    private static final String DIR = "../kjar/src/main/resources/rules/templates/";

    private static final String WATER_BELOW_DRT = DIR + "water-level-below.drt";
    private static final String WATER_RANGE_DRT = DIR + "water-level-range.drt";
    private static final String WATER_ABOVE_DRT = DIR + "water-level-above.drt";

    private static final String WATER_BELOW_XLS = DIR + "water-level-below.xls";
    private static final String WATER_RANGE_XLS = DIR + "water-level-range.xls";
    private static final String WATER_ABOVE_XLS = DIR + "water-level-above.xls";

    @Test
    public void testWaterLevelTemplatesWithSpreadsheet() throws Exception {
        String below = fromXlsGenerateDRL(WATER_BELOW_DRT, WATER_BELOW_XLS);
        String range = fromXlsGenerateDRL(WATER_RANGE_DRT, WATER_RANGE_XLS);
        String above = fromXlsGenerateDRL(WATER_ABOVE_DRT, WATER_ABOVE_XLS);

        System.out.println(below);
        System.out.println(range);
        System.out.println(above);

        doWaterLevelTest(session(below, range, above));
    }

    @Test
    public void testWaterLevelTemplatesWithArrays() throws Exception {

        String below = fromArrayGenerateDRL(WATER_BELOW_DRT, new String[][]{
            new String[]{"RIVER",        "200", "NORMAL"},
            new String[]{"CANAL",        "100", "NORMAL"},
            new String[]{"RESERVOIR",    "500", "NORMAL"},
            new String[]{"PUMP_STATION", "150", "NORMAL"},
        });

        String range = fromArrayGenerateDRL(WATER_RANGE_DRT, new String[][]{
            new String[]{"RIVER",        "200", "350", "ELEVATED"},
            new String[]{"RIVER",        "350", "500", "HIGH"},
            new String[]{"CANAL",        "100", "180", "ELEVATED"},
            new String[]{"CANAL",        "180", "250", "HIGH"},
            new String[]{"RESERVOIR",    "500", "700", "ELEVATED"},
            new String[]{"RESERVOIR",    "700", "850", "HIGH"},
            new String[]{"PUMP_STATION", "150", "250", "ELEVATED"},
            new String[]{"PUMP_STATION", "250", "350", "HIGH"},
        });

        String above = fromArrayGenerateDRL(WATER_ABOVE_DRT, new String[][]{
            new String[]{"RIVER",        "500", "CRITICAL"},
            new String[]{"CANAL",        "250", "CRITICAL"},
            new String[]{"RESERVOIR",    "850", "CRITICAL"},
            new String[]{"PUMP_STATION", "350", "CRITICAL"},
        });

        doWaterLevelTest(session(below, range, above));
    }

    private void doWaterLevelTest(KieSession ksession) {
        ksession.insert(new SensorReading(new Location("RIVER_1", LocationType.RIVER),         SensorType.WATER_LEVEL, 120.0)); // <=200   -> NORMAL
        ksession.insert(new SensorReading(new Location("RIVER_2", LocationType.RIVER),         SensorType.WATER_LEVEL, 300.0)); // 200-350 -> ELEVATED
        ksession.insert(new SensorReading(new Location("RIVER_3", LocationType.RIVER),         SensorType.WATER_LEVEL, 400.0)); // 350-500 -> HIGH
        ksession.insert(new SensorReading(new Location("RIVER_4", LocationType.RIVER),         SensorType.WATER_LEVEL, 600.0)); // >500    -> CRITICAL
        ksession.insert(new SensorReading(new Location("CANAL_1", LocationType.CANAL),         SensorType.WATER_LEVEL,  90.0)); // <=100   -> NORMAL
        ksession.insert(new SensorReading(new Location("CANAL_2", LocationType.CANAL),         SensorType.WATER_LEVEL, 220.0)); // 180-250 -> HIGH
        ksession.insert(new SensorReading(new Location("RESERVOIR_1", LocationType.RESERVOIR), SensorType.WATER_LEVEL, 650.0)); // 500-700 -> ELEVATED
        ksession.insert(new SensorReading(new Location("PUMP_1", LocationType.PUMP_STATION),   SensorType.WATER_LEVEL, 400.0)); // >350    -> CRITICAL

        ksession.fireAllRules();

        Map<String, StatusLevel> result = new HashMap<>();
        for (Object o : ksession.getObjects(new ClassObjectFilter(WaterLevelStatus.class))) {
            WaterLevelStatus s = (WaterLevelStatus) o;
            result.put(s.getLocation().getCode(), s.getLevel());
        }

        for (String key : result.keySet()) {
            System.out.println(key + ": " + result.get(key));
        }

        assertEquals(8, result.size());
        assertEquals(StatusLevel.NORMAL,   result.get("RIVER_1"));
        assertEquals(StatusLevel.ELEVATED, result.get("RIVER_2"));
        assertEquals(StatusLevel.HIGH,     result.get("RIVER_3"));
        assertEquals(StatusLevel.CRITICAL, result.get("RIVER_4"));
        assertEquals(StatusLevel.NORMAL,   result.get("CANAL_1"));
        assertEquals(StatusLevel.HIGH,     result.get("CANAL_2"));
        assertEquals(StatusLevel.ELEVATED, result.get("RESERVOIR_1"));
        assertEquals(StatusLevel.CRITICAL, result.get("PUMP_1"));

        ksession.dispose();
    }


    private static String fromXlsGenerateDRL(String drtPath, String xlsPath) throws Exception {
        return new ExternalSpreadsheetCompiler()
                .compile(new FileInputStream(xlsPath), new FileInputStream(drtPath), 3, 2);
    }

    private static String fromArrayGenerateDRL(String drtPath, String[][] rows) throws Exception {
        return new DataProviderCompiler()
                .compile(new ArrayDataProvider(rows), new FileInputStream(drtPath));
    }

    private KieSession session(String... drls) {
        KieHelper kieHelper = new KieHelper();
        for (String drl : drls) {
            kieHelper.addContent(drl, ResourceType.DRL);
        }
        return kieHelper.build().newKieSession();
    }
}
