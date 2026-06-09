package com.ftn.service.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.internal.utils.KieHelper;
import org.springframework.stereotype.Service;

import com.ftn.model.ThresholdConfig;
import com.ftn.model.enums.ParameterType;

@Service
public class RuleTemplateService {

    private static final String TPL_BELOW = "rules/templates/water-level-below.drt";
    private static final String TPL_RANGE = "rules/templates/water-level-range.drt";
    private static final String TPL_ABOVE = "rules/templates/water-level-above.drt";

    private static final String[] STATIC_DRLS = {
            "rules/group1-classification.drl",
            "rules/group2-assessment.drl",
            "rules/group3-recommendations.drl"
    };

    public KieBase build(List<ThresholdConfig> thresholds) {
        List<Map<String, Object>> belowRows = new ArrayList<>();
        List<Map<String, Object>> rangeRows = new ArrayList<>();
        List<Map<String, Object>> aboveRows = new ArrayList<>();

        for (ThresholdConfig tc : thresholds) {
            if (tc.getParameterType() != ParameterType.WATER_LEVEL || tc.getCriticalMax() == null) {
                continue;
            }
            String locType = tc.getLocationType().name();

            Map<String, Object> normal = new HashMap<>();
            normal.put("locationType", locType);
            normal.put("maxValue", tc.getNormalMax());
            normal.put("statusLevel", "NORMAL");
            belowRows.add(normal);

            Map<String, Object> elevated = new HashMap<>();
            elevated.put("locationType", locType);
            elevated.put("minValue", tc.getNormalMax());
            elevated.put("maxValue", tc.getWarningMax());
            elevated.put("statusLevel", "ELEVATED");
            rangeRows.add(elevated);

            Map<String, Object> high = new HashMap<>();
            high.put("locationType", locType);
            high.put("minValue", tc.getWarningMax());
            high.put("maxValue", tc.getCriticalMax());
            high.put("statusLevel", "HIGH");
            rangeRows.add(high);

            Map<String, Object> critical = new HashMap<>();
            critical.put("locationType", locType);
            critical.put("minValue", tc.getCriticalMax());
            critical.put("statusLevel", "CRITICAL");
            aboveRows.add(critical);
        }

        String belowDrl = compile(belowRows, TPL_BELOW);
        String rangeDrl = compile(rangeRows, TPL_RANGE);
        String aboveDrl = compile(aboveRows, TPL_ABOVE);

        KieHelper helper = new KieHelper();
        helper.addContent(belowDrl, ResourceType.DRL);
        helper.addContent(rangeDrl, ResourceType.DRL);
        helper.addContent(aboveDrl, ResourceType.DRL);
        for (String drl : STATIC_DRLS) {
            helper.addContent(readClasspath(drl), ResourceType.DRL);
        }

        return helper.build();
    }

    private String compile(List<Map<String, Object>> rows, String templatePath) {
        try (InputStream tpl = openClasspath(templatePath)) {
            ObjectDataCompiler compiler = new ObjectDataCompiler();
            return compiler.compile(rows, tpl);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to compile rule template: " + templatePath, e);
        }
    }

    private String readClasspath(String path) {
        try (InputStream is = openClasspath(path)) {
            return new String(readAll(is), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read rule file from classpath: " + path, e);
        }
    }

    private InputStream openClasspath(String path) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is == null) {
            is = getClass().getClassLoader().getResourceAsStream(path);
        }
        if (is == null) {
            throw new IllegalStateException("Classpath resource not found: " + path);
        }
        return is;
    }

    private byte[] readAll(InputStream is) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        while ((n = is.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
        }
        return buffer.toByteArray();
    }
}
