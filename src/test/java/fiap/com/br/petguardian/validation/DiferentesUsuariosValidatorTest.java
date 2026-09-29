package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.usuariopet.dto.TransferirResponsabilidadeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiferentesUsuariosValidatorTest {

    private DiferentesUsuariosValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DiferentesUsuariosValidator();
    }

    @Test
    @DisplayName("Deve validar quando responsavelAtualId e novoResponsavelId sao diferentes")
    void deveValidarUsuariosDiferentes() {
        var request = new TransferirResponsabilidadeRequest(1L, 2L);
        assertTrue(validator.isValid(request, null));
    }

    @Test
    @DisplayName("Deve rejeitar quando responsavelAtualId e novoResponsavelId sao iguais")
    void deveRejeitarUsuariosIguais() {
        var request = new TransferirResponsabilidadeRequest(1L, 1L);
        assertFalse(validator.isValid(request, null));
    }

    @Test
    @DisplayName("Deve aceitar se o objeto ou campos forem nulos deixando para @NotNull")
    void deveAceitarNulos() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid(new TransferirResponsabilidadeRequest(null, 2L), null));
        assertTrue(validator.isValid(new TransferirResponsabilidadeRequest(1L, null), null));
    }
}
