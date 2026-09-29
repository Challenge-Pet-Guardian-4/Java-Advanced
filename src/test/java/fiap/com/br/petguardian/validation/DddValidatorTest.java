package fiap.com.br.petguardian.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DddValidatorTest {

    private DddValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DddValidator();
    }

    @Test
    @DisplayName("Deve validar DDDs validos do Brasil")
    void deveValidarDddValido() {
        assertTrue(validator.isValid("11", null));
        assertTrue(validator.isValid("21", null));
        assertTrue(validator.isValid("31", null));
        assertTrue(validator.isValid("41", null));
        assertTrue(validator.isValid("51", null));
        assertTrue(validator.isValid("61", null));
        assertTrue(validator.isValid("71", null));
        assertTrue(validator.isValid("81", null));
        assertTrue(validator.isValid("91", null));
    }

    @Test
    @DisplayName("Deve aceitar nulo ou branco")
    void deveAceitarNuloOuBranco() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("  ", null));
    }

    @Test
    @DisplayName("Deve rejeitar DDDs invalidos")
    void deveRejeitarDddInvalido() {
        assertFalse(validator.isValid("00", null));
        assertFalse(validator.isValid("01", null));
        assertFalse(validator.isValid("20", null));
        assertFalse(validator.isValid("23", null));
        assertFalse(validator.isValid("111", null));
        assertFalse(validator.isValid("AA", null));
    }
}
