package com.example.namedmoment.exception;

import com.example.namedmoment.dto.Result;
import com.example.namedmoment.enums.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @Test
    void shouldExposeStableErrorCode() {
        BusinessException exception = new BusinessException(ErrorCode.CONCEPT_MATCH_FAILED);

        assertEquals(50002, exception.getErrorCode().getCode());
        assertEquals("概念匹配失败", exception.getMessage());
    }

    @Test
    void shouldBuildSuccessResult() {
        Result<String> result = Result.success("ok");

        assertEquals(0, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("ok", result.getData());
    }

    @Test
    void shouldPreserveTechnicalCauseForDiagnostics() {
        IllegalStateException cause = new IllegalStateException("provider rejected request");

        BusinessException exception = new BusinessException(ErrorCode.FINGERPRINT_FAILED, cause);

        assertEquals(cause, exception.getCause());
    }
}
