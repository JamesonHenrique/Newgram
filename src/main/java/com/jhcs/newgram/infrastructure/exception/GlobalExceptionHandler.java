package com.jhcs.newgram.infrastructure.exception;

import com.jhcs.newgram.infrastructure.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ApiError error(int status, String message, HttpServletRequest request) {
        return new ApiError(status, message, request.getRequestURI(), LocalDateTime.now(),
                UUID.randomUUID().toString());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return new ResponseEntity<>(error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {
        return new ResponseEntity<>(error(HttpStatus.NOT_FOUND.value(), "Recurso não encontrado", request),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorizedException(
            UnauthorizedException ex, HttpServletRequest request) {
        return new ResponseEntity<>(error(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), request),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        return new ResponseEntity<>(error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ArquivoException.class)
    public ResponseEntity<ApiError> handleArquivoException(
            ArquivoException ex, HttpServletRequest request) {
        // Falha de infra (S3 fora) nao e 400 do cliente: distingue pela causa.
        boolean infra = ex.getCause() instanceof java.io.IOException;
        HttpStatus status = infra ? HttpStatus.BAD_GATEWAY : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(error(status.value(), ex.getMessage(), request), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (ObjectError err : ex.getBindingResult().getAllErrors()) {
            if (err instanceof FieldError field) {
                errors.put(field.getField(), field.getDefaultMessage());
            } else {
                errors.put(err.getObjectName(), err.getDefaultMessage());
            }
        }
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                request.getRequestURI(),
                LocalDateTime.now(),
                errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JwtService.JwtAuthenticationException.class)
    public ResponseEntity<ApiError> handleJwtAuthenticationException(
            JwtService.JwtAuthenticationException ex, HttpServletRequest request) {
        return new ResponseEntity<>(error(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), request),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        // Mensagem generica de proposito: nao revelar se o usuario existe.
        return new ResponseEntity<>(error(HttpStatus.UNAUTHORIZED.value(), "Credenciais inválidas", request),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUsernameNotFoundException(
            UsernameNotFoundException ex, HttpServletRequest request) {
        return new ResponseEntity<>(error(HttpStatus.UNAUTHORIZED.value(), "Credenciais inválidas", request),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.warn("Violacao de integridade em {}", request.getRequestURI(), ex);
        return new ResponseEntity<>(
                error(HttpStatus.CONFLICT.value(), "Conflito: registro duplicado ou referência inválida", request),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return new ResponseEntity<>(
                error(HttpStatus.PAYLOAD_TOO_LARGE.value(), "O tamanho do arquivo excede o limite permitido",
                        request),
                HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler({MultipartException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ApiError> handleMultipartException(
            Exception ex, HttpServletRequest request) {
        // Nao vazar detalhe interno do multipart para o cliente.
        logger.warn("Falha no upload em {}", request.getRequestURI(), ex);
        return new ResponseEntity<>(error(HttpStatus.BAD_REQUEST.value(), "Erro no processamento do arquivo",
                request), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ApiError> handleTransactionException(
            TransactionException ex, HttpServletRequest request) {
        logger.error("Falha de transacao em {}", request.getRequestURI(), ex);
        return new ResponseEntity<>(error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor", request), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex, HttpServletRequest request) {
        logger.error("Erro nao tratado em {}", request.getRequestURI(), ex);
        return new ResponseEntity<>(error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor", request), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
