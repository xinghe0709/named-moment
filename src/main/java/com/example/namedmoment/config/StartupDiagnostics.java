package com.example.namedmoment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class StartupDiagnostics {

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:unknown}")
    private String aiBaseUrl;

    @Value("${spring.ai.openai.chat.options.model:unknown}")
    private String chatModel;

    @Value("${spring.ai.openai.embedding.options.model:unknown}")
    private String embeddingModel;

    @EventListener(ApplicationReadyEvent.class)
    public void logConfigurationStatus() {
        boolean apiKeyConfigured = StringUtils.hasText(apiKey)
                && !"not-configured".equals(apiKey);
        log.info("stage=startup-diagnostics status=ready aiKeyConfigured={} "
                        + "aiBaseUrl={} chatModel={} embeddingModel={}",
                apiKeyConfigured, aiBaseUrl, chatModel, embeddingModel);
        if (!apiKeyConfigured) {
            log.warn("stage=startup-diagnostics status=invalid property=AI_API_KEY "
                    + "action=configure-project-dotenv");
        }
    }
}
