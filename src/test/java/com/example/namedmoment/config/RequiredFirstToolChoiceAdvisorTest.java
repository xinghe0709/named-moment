package com.example.namedmoment.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class RequiredFirstToolChoiceAdvisorTest {

    private final RequiredFirstToolChoiceAdvisor advisor =
            new RequiredFirstToolChoiceAdvisor();

    @Test
    void shouldRequireToolOnFirstRound() {
        ChatClientRequest request = requestWithTools(false);

        ChatClientRequest result = advisor.applyToolChoice(request);

        assertEquals("required", options(result).getToolChoice());
    }

    @Test
    void shouldDisableToolsAfterToolResponse() {
        ChatClientRequest request = requestWithTools(true);

        ChatClientRequest result = advisor.applyToolChoice(request);

        assertEquals("none", options(result).getToolChoice());
    }

    @Test
    void shouldNotChangeRequestWithoutTools() {
        OpenAiChatOptions options = OpenAiChatOptions.builder().build();
        ChatClientRequest request = new ChatClientRequest(
                new Prompt(Collections.<Message>singletonList(
                        new UserMessage("普通结构化请求")), options),
                Collections.<String, Object>emptyMap());

        ChatClientRequest result = advisor.applyToolChoice(request);

        assertNull(options(result).getToolChoice());
    }

    private ChatClientRequest requestWithTools(boolean includeToolResponse) {
        ToolCallback toolCallback = mock(ToolCallback.class);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .toolCallbacks(Collections.singletonList(toolCallback))
                .build();
        List<Message> messages = new ArrayList<Message>();
        messages.add(new SystemMessage("调用工具"));
        messages.add(new UserMessage("查询情感概念"));
        if (includeToolResponse) {
            ToolResponseMessage.ToolResponse response =
                    new ToolResponseMessage.ToolResponse(
                            "call-1", "searchEmotionConcepts", "[]");
            messages.add(ToolResponseMessage.builder()
                    .responses(Collections.singletonList(response))
                    .build());
        }
        return new ChatClientRequest(
                new Prompt(messages, options),
                Collections.<String, Object>emptyMap());
    }

    private OpenAiChatOptions options(ChatClientRequest request) {
        return (OpenAiChatOptions) request.prompt().getOptions();
    }
}
