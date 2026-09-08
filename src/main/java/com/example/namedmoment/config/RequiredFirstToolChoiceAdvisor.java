package com.example.namedmoment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RequiredFirstToolChoiceAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request,
                                         CallAdvisorChain chain) {
        return chain.nextCall(applyToolChoice(request));
    }

    ChatClientRequest applyToolChoice(ChatClientRequest request) {
        if (!(request.prompt().getOptions() instanceof OpenAiChatOptions)) {
            log.warn("stage=tool-choice status=skipped optionsType={}",
                    request.prompt().getOptions() == null
                            ? "null" : request.prompt().getOptions().getClass().getSimpleName());
            return request;
        }

        boolean toolAlreadyCalled = false;
        for (Message message : request.prompt().getInstructions()) {
            if (message instanceof ToolResponseMessage) {
                toolAlreadyCalled = true;
                break;
            }
        }

        OpenAiChatOptions originalOptions = (OpenAiChatOptions) request.prompt().getOptions();
        int originalToolCount = originalOptions.getToolCallbacks() == null
                ? 0 : originalOptions.getToolCallbacks().size();
        if (originalToolCount == 0) {
            return request;
        }
        OpenAiChatOptions options = originalOptions
                .mutate()
                .toolChoice(toolAlreadyCalled ? "none" : "required")
                .build();
        int copiedToolCount = options.getToolCallbacks() == null
                ? 0 : options.getToolCallbacks().size();
        log.info("stage=tool-choice status=applied choice={} toolsBefore={} toolsAfter={}",
                toolAlreadyCalled ? "none" : "required",
                originalToolCount, copiedToolCount);
        Prompt prompt = new Prompt(request.prompt().getInstructions(), options);
        return request.mutate().prompt(prompt).build();
    }

    @Override
    public String getName() {
        return "Required First Tool Choice Advisor";
    }

    @Override
    public int getOrder() {
        return ToolCallingAdvisor.DEFAULT_ORDER + 10;
    }
}
