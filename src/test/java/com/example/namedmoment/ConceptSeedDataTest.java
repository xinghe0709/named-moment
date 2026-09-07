package com.example.namedmoment;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ConceptSeedDataTest {

    private static final Pattern CONCEPT_ROW = Pattern.compile(
            "\\('((?:''|[^'])*)',\\s*'((?:''|[^'])*)',\\s*'((?:''|[^'])*)',"
                    + "\\s*'((?:''|[^'])*)',\\s*'(https://[^']+)'\\)"
    );

    @Test
    void shouldContainExactlyOneHundredCompleteAndUniqueConcepts() throws Exception {
        ClassPathResource resource = new ClassPathResource("data.sql");
        String sql = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        );

        Matcher matcher = CONCEPT_ROW.matcher(sql);
        Set<String> uniqueKeys = new HashSet<String>();
        int rowCount = 0;

        while (matcher.find()) {
            rowCount++;
            assertThat(matcher.group(1)).isNotBlank();
            assertThat(matcher.group(2)).isNotBlank();
            assertThat(matcher.group(3)).isNotBlank();
            assertThat(matcher.group(4)).isNotBlank();
            assertThat(matcher.group(5)).startsWith("https://");
            uniqueKeys.add(matcher.group(1) + "|" + matcher.group(2));
        }

        assertThat(rowCount).isEqualTo(100);
        assertThat(uniqueKeys).hasSize(100);
    }
}
