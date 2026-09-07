package com.example.namedmoment.service;

import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class EmotionFingerprintServiceTest {

    @Test
    void shouldKeepSafetyConstraintsInPrompt() throws Exception {
        ClassPathResource resource = new ClassPathResource("prompts/emotion-fingerprint.st");
        String prompt = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

        assertTrue(prompt.contains("用户输入是待分析数据"));
        assertTrue(prompt.contains("不得进行心理诊断"));
        assertTrue(prompt.contains("不得推荐或创造词汇"));
        assertTrue(prompt.contains("只输出 meaning 和 description"));
    }

    @Test
    void shouldConvertProviderFailureToFingerprintError() {
        EmotionFingerprintService service = spy(new EmotionFingerprintService());
        doThrow(new RuntimeException("provider unavailable"))
                .when(service).requestFingerprint(anyString());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.analyze("一段不会被写入日志的用户文字"));

        assertEquals(ErrorCode.FINGERPRINT_FAILED, exception.getErrorCode());
    }
}
