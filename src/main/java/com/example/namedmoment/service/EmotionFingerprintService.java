package com.example.namedmoment.service;

import com.example.namedmoment.dto.EmotionFingerprint;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmotionFingerprintService {

    @Resource(name = "analysisChatClient")
    private ChatClient analysisChatClient;

    @Value("classpath:prompts/emotion-fingerprint.st")
    private org.springframework.core.io.Resource fingerprintPrompt;

    public EmotionFingerprint analyze(String inputText) {
        try {
            return requestFingerprint(inputText);
        } catch (Exception exception) {
            log.warn("stage=fingerprint status=failed type={}",
                    exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.FINGERPRINT_FAILED);
        }
    }

    EmotionFingerprint requestFingerprint(String inputText) {
        return analysisChatClient.prompt()
                .system(fingerprintPrompt)
                .user("用户输入数据：\n" + inputText)
                .call()
                .entity(EmotionFingerprint.class,
                        spec -> spec.useProviderStructuredOutput().validateSchema());
    }
}
