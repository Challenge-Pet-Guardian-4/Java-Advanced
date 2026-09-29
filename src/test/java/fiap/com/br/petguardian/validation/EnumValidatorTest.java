package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.pet.PetPorte;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.usuario.UsuarioRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class EnumValidatorTest {

    @Test
    @DisplayName("Deve validar valores validos de PetPorte")
    void deveValidarPetPorte() {
        EnumValidator validator = new EnumValidator();
        EnumValidation annotation = Mockito.mock(EnumValidation.class);
        when(annotation.enumClass()).thenAnswer(inv -> PetPorte.class);
        validator.initialize(annotation);

        assertTrue(validator.isValid("PEQUENO", null));
        assertTrue(validator.isValid("medio", null));
        assertTrue(validator.isValid("  GRANDE  ", null));
        assertFalse(validator.isValid("GIGANTE", null));
    }

    @Test
    @DisplayName("Deve validar valores validos de UsuarioRole")
    void deveValidarUsuarioRole() {
        EnumValidator validator = new EnumValidator();
        EnumValidation annotation = Mockito.mock(EnumValidation.class);
        when(annotation.enumClass()).thenAnswer(inv -> UsuarioRole.class);
        validator.initialize(annotation);

        assertTrue(validator.isValid("COMUM", null));
        assertTrue(validator.isValid("PREMIUM", null));
        assertTrue(validator.isValid("ADMIN", null));
        assertFalse(validator.isValid("INVALIDO", null));
    }

    @Test
    @DisplayName("Deve validar valores validos de EnumStatus")
    void deveValidarEnumStatus() {
        EnumValidator validator = new EnumValidator();
        EnumValidation annotation = Mockito.mock(EnumValidation.class);
        when(annotation.enumClass()).thenAnswer(inv -> EnumStatus.class);
        validator.initialize(annotation);

        assertTrue(validator.isValid("PENDENTE", null));
        assertTrue(validator.isValid("CONCLUIDO", null));
        assertTrue(validator.isValid("EXPIRADO", null));
        assertFalse(validator.isValid("CANCELADO", null));
    }

    @Test
    @DisplayName("Deve aceitar valor nulo deixando para @NotNull")
    void deveAceitarNulo() {
        EnumValidator validator = new EnumValidator();
        EnumValidation annotation = Mockito.mock(EnumValidation.class);
        when(annotation.enumClass()).thenAnswer(inv -> EnumStatus.class);
        validator.initialize(annotation);

        assertTrue(validator.isValid(null, null));
    }
}
