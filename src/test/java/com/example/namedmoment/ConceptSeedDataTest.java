package com.example.namedmoment;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ConceptSeedDataTest {

    private static final int EXPECTED_CONCEPT_COUNT = 500;
    private static final int MINIMUM_LANGUAGE_COUNT = 40;
    private static final Set<String> TRUSTED_SOURCE_HOSTS = Set.of(
            "hifisamurai.github.io",
            "www.drtimlomas.com",
            "doi.org"
    );

    private static final Pattern CONCEPT_ROW = Pattern.compile(
            "\\('((?:''|[^'])*)',\\s*'((?:''|[^'])*)',\\s*'((?:''|[^'])*)',"
                    + "\\s*'((?:''|[^'])*)',\\s*'(https://[^']+)'\\)"
    );

    @Test
    void shouldContainFiveHundredCompleteAndUniqueConcepts() throws Exception {
        ClassPathResource resource = new ClassPathResource("data.sql");
        String sql = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        );

        Matcher matcher = CONCEPT_ROW.matcher(sql);
        Set<String> uniqueKeys = new HashSet<String>();
        Set<String> languages = new HashSet<String>();
        int rowCount = 0;

        while (matcher.find()) {
            rowCount++;
            String name = unescapeSql(matcher.group(1));
            String language = unescapeSql(matcher.group(2));
            String meaning = unescapeSql(matcher.group(3));
            String description = unescapeSql(matcher.group(4));
            String sourceUrl = matcher.group(5);

            assertThat(name).isNotBlank().hasSizeLessThanOrEqualTo(100);
            assertThat(language).isNotBlank().hasSizeLessThanOrEqualTo(50);
            assertThat(meaning)
                    .isNotBlank()
                    .isNotIn("待确认", "暂无")
                    .doesNotContain("TODO", "这个词", "该概念", "字面含义")
                    .doesNotMatch(".*[A-Za-z]{3,}.*");
            assertThat(description)
                    .isNotBlank()
                    .isNotIn("待确认", "暂无")
                    .doesNotContain("TODO", "这个词", "该概念", "字面含义")
                    .doesNotMatch(".*[A-Za-z]{3,}.*");
            assertThat(sourceUrl).startsWith("https://");
            assertThat(TRUSTED_SOURCE_HOSTS).contains(URI.create(sourceUrl).getHost());

            uniqueKeys.add(name.toLowerCase() + "|" + language);
            languages.add(language);
        }

        assertThat(rowCount).isEqualTo(EXPECTED_CONCEPT_COUNT);
        assertThat(uniqueKeys).hasSize(EXPECTED_CONCEPT_COUNT);
        assertThat(languages).hasSizeGreaterThanOrEqualTo(MINIMUM_LANGUAGE_COUNT);
    }

    private String unescapeSql(String value) {
        return value.replace("''", "'");
    }
}
