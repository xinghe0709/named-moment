package com.example.namedmoment.exception;

import com.example.namedmoment.dto.Result;
import com.example.namedmoment.enums.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<Result<Void>> handleValidation(Exception exception) {
        log.warn("stage=request-validation status=failed type={}",
                exception.getClass().getSimpleName());
        return ResponseEntity.badRequest().body(Result.failure(ErrorCode.PARAM_ERROR));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException exception) {
        log.warn("stage=business status=failed type={}",
                exception.getClass().getSimpleName());
        return ResponseEntity.status(statusOf(exception.getErrorCode()))
                .body(Result.failure(exception.getErrorCode()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Result<Void>> handleDatabase(DataAccessException exception) {
        log.warn("stage=database status=failed type={}",
                exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.failure(ErrorCode.DATABASE_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpected(Exception exception) {
        log.warn("stage=unexpected status=failed type={}",
                exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.failure(ErrorCode.CONCEPT_MATCH_FAILED));
    }

    private HttpStatus statusOf(ErrorCode errorCode) {
        if (ErrorCode.PARAM_ERROR == errorCode) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ErrorCode.RECORD_NOT_FOUND == errorCode) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
