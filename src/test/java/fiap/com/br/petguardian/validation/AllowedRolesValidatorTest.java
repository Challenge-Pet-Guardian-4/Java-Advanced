package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.usuario.UsuarioRole;
import jakarta.validation.Payload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllowedRolesValidatorTest {

    private AllowedRolesValidator buildValidator(UsuarioRole... allowed) {
        AllowedRolesValidator validator = new AllowedRolesValidator();
        validator.initialize(new AllowedRolesValidation() {
            @Override
            public UsuarioRole[] allowed() {
                return allowed;
            }

            @Override
            public String message() {
                return "Role não permitido para este endpoint.";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return AllowedRolesValidation.class;
            }
        });
        return validator;
    }

    @Test
    @DisplayName("Deve aceitar COMUM quando permitido")
    void deveAceitarCOMUM() {
        assertTrue(buildValidator(UsuarioRole.COMUM, UsuarioRole.PREMIUM).isValid("COMUM", null));
    }

    @Test
    @DisplayName("Deve aceitar PREMIUM quando permitido")
    void deveAceitarPREMIUM() {
        assertTrue(buildValidator(UsuarioRole.COMUM, UsuarioRole.PREMIUM).isValid("PREMIUM", null));
    }

    @Test
    @DisplayName("Deve aceitar valor case-insensitive e com espacos")
    void deveAceitarCaseInsensitive() {
        AllowedRolesValidator v = buildValidator(UsuarioRole.COMUM, UsuarioRole.PREMIUM);
        assertTrue(v.isValid("  comum  ", null));
        assertTrue(v.isValid("premium", null));
    }

    @Test
    @DisplayName("Deve rejeitar ADMIN quando nao esta na lista de permitidos")
    void deveRejeitarADMIN() {
        assertFalse(buildValidator(UsuarioRole.COMUM, UsuarioRole.PREMIUM).isValid("ADMIN", null));
    }

    @Test
    @DisplayName("Deve rejeitar valor invalido")
    void deveRejeitarValorInvalido() {
        assertFalse(buildValidator(UsuarioRole.COMUM, UsuarioRole.PREMIUM).isValid("SUPERADMIN", null));
    }

    @Test
    @DisplayName("Deve aceitar null delegando para @NotBlank")
    void deveAceitarNull() {
        assertTrue(buildValidator(UsuarioRole.COMUM, UsuarioRole.PREMIUM).isValid(null, null));
    }
}
