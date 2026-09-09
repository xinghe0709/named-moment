package com.example.namedmoment;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontendResourceTest {

    @Test
    void exposesAccessibleSinglePageEntry() throws IOException {
        String html = readResource("static/index.html");

        assertTrue(html.contains("<html lang=\"zh-CN\""));
        assertTrue(html.contains("name=\"viewport\""));
        assertTrue(html.contains("<main"));
        assertTrue(html.contains("<form"));
        assertTrue(html.contains("aria-live=\"polite\""));
        assertTrue(html.contains("type=\"module\""));
        assertTrue(html.contains("67ec61a6"));
    }

    @Test
    void includesResponsiveAndReducedMotionStyles() throws IOException {
        String css = readResource("static/css/styles.css");

        assertTrue(css.contains(":focus-visible"));
        assertTrue(css.contains("prefers-reduced-motion: reduce"));
        assertTrue(css.contains("@media (max-width:"));
        assertTrue(css.contains("@media (max-height: 820px)"),
                "short desktop viewports need a dedicated composition");
        assertTrue(css.contains("--memory-cobalt: #1646b8"));
        assertFalse(css.contains("min-height: 660px"),
                "fixed pane heights push the primary action below short desktop viewports");
    }

    @Test
    void packagesFrontendModulesAndGlassTexture() throws IOException {
        assertNotNull(getClass().getClassLoader().getResource("static/js/domain.mjs"));
        assertNotNull(getClass().getClassLoader().getResource("static/js/api.mjs"));
        assertNotNull(getClass().getClassLoader().getResource("static/js/app.mjs"));
        assertNotNull(getClass().getClassLoader().getResource("static/assets/seeded-glass.png"));

        String api = readResource("static/js/api.mjs");
        assertTrue(api.contains("/api/emotions/match"));
        assertTrue(api.contains("/api/emotions/records"));
    }

    @Test
    void keepsAnalysisAndFeedbackInsideTheirActiveGlassPanes() throws IOException {
        String html = readResource("static/index.html");
        String script = readResource("static/js/app.mjs");

        int composePaneStart = html.indexOf("<div class=\"compose-pane glass-pane\">");
        int matchButtonStart = html.indexOf("<button id=\"matchButton\"");
        int analysisStatus = html.indexOf("id=\"analysisStatus\"");

        assertTrue(composePaneStart >= 0 && analysisStatus > composePaneStart && analysisStatus < matchButtonStart);
        assertTrue(script.contains("candidate-status"));
        assertTrue(script.contains("archive-record-status"));
    }

    @Test
    void preservesTheApprovedUnbrandedArchitectureAndAuthoredSelectionMotion() throws IOException {
        String html = readResource("static/index.html");
        String css = readResource("static/css/styles.css");

        assertFalse(html.contains("class=\"brand-mark\""));
        assertTrue(css.contains("transition: filter 720ms"));
        assertFalse(css.contains("border-left: 4px solid currentColor"));
    }

    private String readResource(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        assertTrue(resource.exists(), path + " should exist");
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}
