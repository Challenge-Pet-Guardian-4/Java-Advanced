package fiap.com.br.petguardian.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ValidationErrorDetail(String campo, String mensagem) {
        public ValidationErrorDetail(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }

        public ValidationErrorDetail(ObjectError error) {
            this(error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName(), error.getDefaultMessage());
        }
    }

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<ValidationErrorDetail>>> handleValidation(
            MethodArgumentNotValidException exception) {
        log.warn("Falha de validacao de entrada detectada: {} erro(s)", exception.getErrorCount());

        List<ValidationErrorDetail> errors = exception.getAllErrors().stream()
                .map(ValidationErrorDetail::new)
                .toList();

        return ResponseEntity.badRequest().body(Map.of("erros", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, List<ValidationErrorDetail>>> handleConstraintViolation(
            ConstraintViolationException exception) {
        log.warn("Falha de validacao de constraint: {}", exception.getMessage());
        List<ValidationErrorDetail> errors = exception.getConstraintViolations().stream()
                .map(cv -> new ValidationErrorDetail(
                        cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : "parametro",
                        cv.getMessage()
                ))
                .toList();
        return ResponseEntity.badRequest().body(Map.of("erros", errors));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception, HttpServletRequest request) {
        log.warn("Recurso nao encontrado: {}", exception.getMessage());
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception, HttpServletRequest request) {
        log.warn("Violacao de regra de negocio: {}", exception.getMessage());
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException exception, HttpServletRequest request) {
        log.warn("Falha de autenticacao para a requisicao {}", request.getRequestURI());
        return error(HttpStatus.UNAUTHORIZED, "Credenciais invalidas.", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception, HttpServletRequest request) {
        log.warn("Acesso negado para a requisicao {}", request.getRequestURI());
        return error(HttpStatus.FORBIDDEN, "Voce nao possui permissao para este recurso.", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        log.warn("JSON com formato ou tipos incorretos: {}", exception.getMessage());
        String mensagem = "Erro na leitura dos dados. Verifique os tipos enviados no JSON.";

        Throwable cause = exception.getCause();
        if (cause instanceof JsonMappingException jme) {
            String field = jme.getPath().isEmpty() ? "campo" : jme.getPath().get(0).getFieldName();
            String fullMessage = exception.getMessage() != null ? exception.getMessage() : "";
            if (fullMessage.contains("DateTimeParseException") || fullMessage.contains("LocalDate") || fullMessage.contains("LocalDateTime")) {
                mensagem = String.format("Data inválida no campo '%s'. Informe uma data de calendário real no formato AAAA-MM-DD.", field);
            } else {
                mensagem = String.format("Valor inválido para o campo '%s'. Verifique o tipo ou formato informado.", field);
            }
        } else if (exception.getMessage() != null && exception.getMessage().contains("DateTimeParseException")) {
            mensagem = "Data informada possui formato ou valor de calendário inválido (dia ou mês inexistente).";
        }

        return error(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        log.error("Erro de consistencia/integridade no banco de dados", exception);
        return error(HttpStatus.BAD_REQUEST,
                "Erro de integridade de dados. Verifique os valores e chaves unicas.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception exception, HttpServletRequest request) {
        log.error("Erro inesperado capturado no handler global", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado no servidor.", request);
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(response);
    }
}
