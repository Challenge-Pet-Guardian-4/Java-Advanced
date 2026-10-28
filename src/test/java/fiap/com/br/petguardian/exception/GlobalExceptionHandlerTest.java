package fiap.com.br.petguardian.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/test");
    }

    @Test
    @DisplayName("Deve tratar ResourceNotFoundException com status 404")
    void deveTratarResourceNotFound() {
        var ex = new ResourceNotFoundException("Item não encontrado");
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
        assertEquals("Item não encontrado", response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException com status 400")
    void deveTratarIllegalArgument() {
        var ex = new IllegalArgumentException("Argumento inválido");
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleIllegalArgument(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Argumento inválido", response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar AuthenticationException com status 401")
    void deveTratarAuthenticationException() {
        var ex = new BadCredentialsException("Falha de autenticação");
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleAuthentication(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Credenciais inválidas.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar AccessDeniedException com status 403")
    void deveTratarAccessDeniedException() {
        var ex = new AccessDeniedException("Proibido");
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Você não possui permissão para este recurso.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotReadableException com status 400")
    void deveTratarHttpMessageNotReadable() {
        var ex = new HttpMessageNotReadableException("JSON malformado", new MockHttpInputMessage(new byte[0]));
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleHttpMessageNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar HttpRequestMethodNotSupportedException com status 405")
    void deveTratarMethodNotSupported() {
        var ex = new HttpRequestMethodNotSupportedException("POST", List.of("GET", "PUT"));
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleMethodNotSupported(ex, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar HttpMediaTypeNotSupportedException com status 415")
    void deveTratarMediaTypeNotSupported() {
        var ex = new HttpMediaTypeNotSupportedException("Content-type inválido");
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleMediaTypeNotSupported(ex, request);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar MissingServletRequestParameterException com status 400")
    void deveTratarMissingParameter() {
        var ex = new MissingServletRequestParameterException("status", "String");
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleMissingParameter(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar RestClientException com status 503")
    void deveTratarRestClientException() {
        var ex = new RestClientException("ViaCEP fora do ar");
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleRestClient(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar DataIntegrityViolationException com duplicidade com status 409")
    void deveTratarDataIntegrityDuplicidade() {
        var cause = new RuntimeException("ORA-00001: unique constraint violated");
        var ex = new DataIntegrityViolationException("Unique error", cause);
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar DataIntegrityViolationException com tamanho excessivo com status 400")
    void deveTratarDataIntegrityTamanho() {
        var cause = new RuntimeException("ORA-12899: value too large for column");
        var ex = new DataIntegrityViolationException("Size error", cause);
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar JpaSystemException com status 500")
    void deveTratarJpaSystemException() {
        var ex = new JpaSystemException(new RuntimeException("JPA crash"));
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleJpaSystem(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar NoResourceFoundException com status 404")
    void deveTratarNoResourceFoundException() {
        var ex = new NoResourceFoundException(HttpMethod.GET, "/inexistente", "/inexistente");
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleNoResourceFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar Exception generica com status 500")
    void deveTratarGenericException() {
        var ex = new RuntimeException("Bug inesperado");
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response = handler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar ConstraintViolationException com status 400")
    void deveTratarConstraintViolation() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("email");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("não pode ser vazio");

        var ex = new ConstraintViolationException(Set.of(violation));
        ResponseEntity<Map<String, List<GlobalExceptionHandler.ValidationErrorDetail>>> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1, response.getBody().get("erros").size());
        assertEquals("email", response.getBody().get("erros").get(0).campo());
    }

    @Test
    @DisplayName("Deve instanciar ValidationErrorDetail a partir de FieldError e ObjectError")
    void deveInstanciarValidationErrorDetail() {
        FieldError fieldError = new FieldError("usuario", "nome", "Nome obrigatório");
        var detail1 = new GlobalExceptionHandler.ValidationErrorDetail(fieldError);
        assertEquals("nome", detail1.campo());
        assertEquals("Nome obrigatório", detail1.mensagem());

        ObjectError objectError = new ObjectError("usuario", "Objeto inválido");
        var detail2 = new GlobalExceptionHandler.ValidationErrorDetail(objectError);
        assertEquals("usuario", detail2.campo());
        assertEquals("Objeto inválido", detail2.mensagem());
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotReadable com JsonMappingException de data e de campo comum")
    void deveTratarHttpMessageNotReadableComJsonMappingException() {
        var jmeDate = com.fasterxml.jackson.databind.JsonMappingException.from(
                (com.fasterxml.jackson.core.JsonParser) null, "DateTimeParseException: valor invalido");
        jmeDate.prependPath(new com.fasterxml.jackson.databind.JsonMappingException.Reference("dataNasc", "dataNasc"));
        var ex1 = new HttpMessageNotReadableException("DateTimeParseException", jmeDate, new MockHttpInputMessage(new byte[0]));
        var resp1 = handler.handleHttpMessageNotReadable(ex1, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp1.getStatusCode());
        org.junit.jupiter.api.Assertions.assertTrue(resp1.getBody().message().contains("dataNasc"));

        var jmeField = com.fasterxml.jackson.databind.JsonMappingException.from(
                (com.fasterxml.jackson.core.JsonParser) null, "Cannot deserialize value");
        jmeField.prependPath(new com.fasterxml.jackson.databind.JsonMappingException.Reference("pontos", "pontos"));
        var ex2 = new HttpMessageNotReadableException("Deserialization failed", jmeField, new MockHttpInputMessage(new byte[0]));
        var resp2 = handler.handleHttpMessageNotReadable(ex2, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp2.getStatusCode());
        org.junit.jupiter.api.Assertions.assertTrue(resp2.getBody().message().contains("pontos"));

        var ex3 = new HttpMessageNotReadableException("DateTimeParseException: erro geral", new MockHttpInputMessage(new byte[0]));
        var resp3 = handler.handleHttpMessageNotReadable(ex3, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp3.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar HandlerMethodValidationException com status 400")
    void deveTratarHandlerMethodValidation() {
        org.springframework.web.method.annotation.HandlerMethodValidationException ex = mock(org.springframework.web.method.annotation.HandlerMethodValidationException.class);
        org.springframework.context.MessageSourceResolvable resolvable = mock(org.springframework.context.MessageSourceResolvable.class);
        when(resolvable.getDefaultMessage()).thenReturn("ID deve ser positivo");
        org.mockito.Mockito.doReturn(List.of(resolvable)).when(ex).getAllErrors();

        var response = handler.handleHandlerMethodValidation(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ID deve ser positivo", response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar DataIntegrityViolationException com ORA-12899 e genérico")
    void deveTratarDataIntegrityViolationComOra12899EGenerico() {
        var ex1 = new DataIntegrityViolationException("Erro", new RuntimeException("ORA-12899: value too large for column"));
        var resp1 = handler.handleDataIntegrityViolation(ex1, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp1.getStatusCode());
        org.junit.jupiter.api.Assertions.assertTrue(resp1.getBody().message().contains("limite máximo permitido"));

        var ex2 = new DataIntegrityViolationException("Erro", new RuntimeException("outra falha qualquer"));
        var resp2 = handler.handleDataIntegrityViolation(ex2, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp2.getStatusCode());
        org.junit.jupiter.api.Assertions.assertTrue(resp2.getBody().message().contains("Erro de integridade de dados"));
    }

    @Test
    @DisplayName("Deve tratar JpaSystemException com ORA-12899, ORA-20002, ORA-00001 e generico")
    void deveTratarJpaSystemComDiversosCodigos() {
        var ex1 = new JpaSystemException(new RuntimeException("ORA-12899: value too large"));
        var resp1 = handler.handleJpaSystem(ex1, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp1.getStatusCode());

        var ex2 = new JpaSystemException(new RuntimeException("ORA-00001: unique constraint violated"));
        var resp2 = handler.handleJpaSystem(ex2, request);
        assertEquals(HttpStatus.CONFLICT, resp2.getStatusCode());

        var ex3 = new JpaSystemException(new RuntimeException("Erro desconhecido"));
        var resp3 = handler.handleJpaSystem(ex3, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp3.getStatusCode());
    }
}
