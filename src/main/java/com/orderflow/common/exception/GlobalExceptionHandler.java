package com.orderflow.common.exception;

import com.orderflow.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation error: {}", errorMessage);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.failure(errorMessage, "ERR_VALIDATION"));
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException ex) {
        // İstemcinin dil ayarına (Accept-Language) göre çeviriyi alıyoruz.
        String localizedMessage = getLocalizedMessage(ex.getMessage());

        log.warn("Business rule violation: {} - {}", ex.getErrorCode(), localizedMessage);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.failure(localizedMessage, ex.getErrorCode()));
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String localizedMessage = getLocalizedMessage(ex.getMessage());

        log.warn("Resource not found: {}", localizedMessage);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Result.failure(localizedMessage, "ERR_NOT_FOUND"));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleAllUncaughtException(Exception ex) {

        log.error("Unexpected error occurred: ", ex);

        String localizedMessage = getLocalizedMessage("system.error.unexpected");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.failure(localizedMessage, "ERR_SYSTEM"));
    }


    private String getLocalizedMessage(String messageKey) {
        try {

            return messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {

            return messageKey;
        }
    }
    // 5. Oturum açmamış kullanıcı hatası (401 Unauthorized)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<Void>> handleUnauthorizedException(UnauthorizedException ex) {
        String localizedMessage = getLocalizedMessage(ex.getMessage());

        log.warn("Unauthorized access attempt: {}", localizedMessage);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.failure(localizedMessage, "ERR_UNAUTHORIZED"));
    }

    // 6. Yetkisiz erişim hatası - Örn: Başkasının siparişini iptal etme (403 Forbidden)
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Result<Void>> handleForbiddenException(ForbiddenException ex) {
        String localizedMessage = getLocalizedMessage(ex.getMessage());

        log.warn("Forbidden access attempt: {}", localizedMessage);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Result.failure(localizedMessage, "ERR_FORBIDDEN"));
    }
}