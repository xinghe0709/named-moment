package com.example.namedmoment.config;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Resource
    private ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    @Resource
    private RequiredFirstToolChoiceAdvisor requiredFirstToolChoiceAdvisor;

    @Bean("analysisChatClient")
    public ChatClient analysisChatClient() {
        return chatClientBuilderProvider.getObject().build();
    }

    @Bean("fallbackChatClient")
    public ChatClient fallbackChatClient() {
        return chatClientBuilderProvider.getObject()
                .defaultAdvisors(requiredFirstToolChoiceAdvisor)
                .build();
    }
}
