package com.claro.desafiopedidos.exception;

import com.claro.desafiopedidos.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundExecption.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundExecption exception, HttpServletRequest request) {
        log.warn(
                "event=resource_not_found method={} path={} status={} message={}",
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage()
        );

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(BusinessExecption.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessExecption exception, HttpServletRequest request) {
        log.warn(
                "event=business_rule_violation method={} path={} status={} message={}",
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                exception.getMessage()
        );

        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(UnauthorizedExecption.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedException(UnauthorizedExecption exception, HttpServletRequest request) {
        log.warn(
                "event=unauthorized_access method={} path={} status={}",
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED.value()
        );

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (firstMessage, secondMessage) -> firstMessage,
                        LinkedHashMap::new
                ));

        log.warn(
                "event=request_validation_failed method={} path={} status={} fields={}",
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                fieldErrors.keySet()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Existem campos inválidos na requisição",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        log.warn(
                "event=request_body_invalid method={} path={} status={} reason={}",
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                exception.getClass().getSimpleName()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição está inválido ou possui valores não reconhecidos",
                request,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error(
                "event=unexpected_error method={} path={} status={} reason={}",
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                exception.getClass().getSimpleName(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno no servidor",
                request,
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}
