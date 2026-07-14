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
                "Recurso não encontrado: method={}, path={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
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
                "Regra de negócio violada: method={}, path={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
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
                "Tentativa de acesso não autorizado: method={}, path={}",
                request.getMethod(),
                request.getRequestURI()
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
                "Falha de validação: method={}, path={}, fields={}",
                request.getMethod(),
                request.getRequestURI(),
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
                "Corpo da requisição inválido: method={}, path={}",
                request.getMethod(),
                request.getRequestURI()
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
                "Erro inesperado: method={}, path={}",
                request.getMethod(),
                request.getRequestURI(),
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