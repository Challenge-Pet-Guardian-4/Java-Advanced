package fiap.com.br.petguardian.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataNascValidatorTest {

    private DataNascValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DataNascValidator();
    }

    @Test
    @DisplayName("Deve validar datas de nascimento dentro do limite de 30 anos")
    void deveValidarDataDentroDoLimite() {
        assertTrue(validator.isValid(LocalDate.now(), null));
        assertTrue(validator.isValid(LocalDate.now().minusYears(5), null));
        assertTrue(validator.isValid(LocalDate.now().minusYears(30), null));
    }

    @Test
    @DisplayName("Deve aceitar nulo deixando obrigatoriedade para @NotNull")
    void deveAceitarNulo() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    @DisplayName("Deve rejeitar datas de nascimento superiores a 30 anos")
    void deveRejeitarDataAcimaDoLimite() {
        assertFalse(validator.isValid(LocalDate.now().minusYears(30).minusDays(1), null));
        assertFalse(validator.isValid(LocalDate.now().minusYears(200), null));
    }
}
