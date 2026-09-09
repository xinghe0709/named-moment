package com.example.namedmoment.service;

import com.example.namedmoment.dto.EmotionFingerprint;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import com.example.namedmoment.utils.ExceptionLogUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmotionFingerprintService {

    @Resource(name = "analysisChatClient")
    private ChatClient analysisChatClient;

    @Value("classpath:prompts/emotion-fingerprint.st")
    private org.springframework.core.io.Resource fingerprintPrompt;

    public EmotionFingerprint analyze(String inputText) {
        long startedAt = System.nanoTime();
        log.info("stage=fingerprint status=started inputLength={}", inputText.length());
        try {
            EmotionFingerprint fingerprint = requestFingerprint(inputText);
            log.info("stage=fingerprint status=success durationMs={}", elapsedMs(startedAt));
            return fingerprint;
        } catch (Exception exception) {
            log.warn("stage=fingerprint status=failed type={} rootType={} "
                            + "rootMessage={} durationMs={}",
                    exception.getClass().getSimpleName(),
                    ExceptionLogUtils.rootType(exception),
                    ExceptionLogUtils.rootMessage(exception), elapsedMs(startedAt));
            throw new BusinessException(ErrorCode.FINGERPRINT_FAILED, exception);
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

    private long elapsedMs(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
