package fiap.com.br.petguardian.tarefa.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TarefaRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Deve aceitar TarefaRequest com título e descrição dentro dos limites")
    void deveAceitarTarefaComCamposValidos() {
        var request = new TarefaRequest(
                "Título com 30 caracteres exato", // 30 chars
                5,
                "Descrição válida",
                LocalDateTime.now().plusDays(1),
                1L,
                "PENDENTE",
                null
        );

        var violations = validator.validate(request);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("titulo")));
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("descricao")));
    }

    @Test
    @DisplayName("Deve rejeitar TarefaRequest quando o título exceder 30 caracteres")
    void deveRejeitarTituloComMaisDe30Caracteres() {
        var request = new TarefaRequest(
                "Título com mais de 30 caracteres!", // 33 chars
                5,
                "Descrição válida",
                LocalDateTime.now().plusDays(1),
                1L,
                "PENDENTE",
                null
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("titulo") &&
                v.getMessage().contains("no máximo 30 caracteres")
        ));
    }

    @Test
    @DisplayName("Deve rejeitar TarefaRequest quando a descrição exceder 200 caracteres")
    void deveRejeitarDescricaoComMaisDe200Caracteres() {
        String descricaoLonga = "A".repeat(201);
        var request = new TarefaRequest(
                "Título normal",
                5,
                descricaoLonga,
                LocalDateTime.now().plusDays(1),
                1L,
                "PENDENTE",
                null
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("descricao") &&
                v.getMessage().contains("no máximo 200 caracteres")
        ));
    }
}
