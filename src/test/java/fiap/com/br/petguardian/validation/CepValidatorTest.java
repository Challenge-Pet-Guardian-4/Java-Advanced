package fiap.com.br.petguardian.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CepValidatorTest {

    private CepValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CepValidator();
    }

    @Test
    @DisplayName("Deve validar CEPs com 8 digitos numericos")
    void deveValidarCepNumerico() {
        assertTrue(validator.isValid("01310100", null));
        assertTrue(validator.isValid("01311000", null));
    }

    @Test
    @DisplayName("Deve validar CEPs com hifen no padrao 5+3")
    void deveValidarCepComHifen() {
        assertTrue(validator.isValid("01310-100", null));
    }

    @Test
    @DisplayName("Deve aceitar nulo ou em branco deixando para @NotBlank")
    void deveAceitarNuloOuBranco() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("   ", null));
    }

    @Test
    @DisplayName("Deve rejeitar CEPs invalidos")
    void deveRejeitarCepInvalido() {
        assertFalse(validator.isValid("1234567", null));
        assertFalse(validator.isValid("123456789", null));
        assertFalse(validator.isValid("01310-10", null));
        assertFalse(validator.isValid("ABCDE-FGH", null));
    }
}
