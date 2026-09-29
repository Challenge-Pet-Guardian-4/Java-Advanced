package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuariopet.UsuarioPet;
import fiap.com.br.petguardian.usuariopet.UsuarioPetId;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioPetValidatorTest {

    @Mock
    private UsuarioPetRepository usuarioPetRepository;

    @InjectMocks
    private UsuarioPetValidator validator;

    @Test
    @DisplayName("Deve permitir se for o responsavel principal")
    void devePermitirResponsavelPrincipal() {
        when(usuarioPetRepository.isResponsavelPrincipal(1L, 10L)).thenReturn(true);

        assertDoesNotThrow(() -> validator.validarResponsavelPrincipal(1L, 10L));
    }

    @Test
    @DisplayName("Deve lançar exceção se não for o responsável principal")
    void deveRejeitarNaoResponsavelPrincipal() {
        when(usuarioPetRepository.isResponsavelPrincipal(1L, 10L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> validator.validarResponsavelPrincipal(1L, 10L));
    }

    @Test
    @DisplayName("Deve permitir convite se o usuário ainda não for vinculado")
    void devePermitirUsuarioNaoVinculado() {
        when(usuarioPetRepository.existsByUsuarioIdAndPetId(2L, 10L)).thenReturn(false);

        assertDoesNotThrow(() -> validator.validarUsuarioNaoVinculado(2L, 10L));
    }

    @Test
    @DisplayName("Deve lançar exceção ao convidar usuário já vinculado")
    void deveRejeitarUsuarioJaVinculado() {
        when(usuarioPetRepository.existsByUsuarioIdAndPetId(2L, 10L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> validator.validarUsuarioNaoVinculado(2L, 10L));
    }

    @Test
    @DisplayName("Não deve permitir desvincular o responsável principal diretamente")
    void deveRejeitarDesvinculacaoResponsavelPrincipal() {
        UsuarioPet vinculoPrincipal = UsuarioPet.builder()
                .id(new UsuarioPetId(1L, 10L))
                .usuario(Usuario.builder().id(1L).build())
                .pet(Pet.builder().id(10L).build())
                .responsavelPrincipal(true)
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validarPermissaoDesvinculacao(vinculoPrincipal, 1L));
    }

    @Test
    @DisplayName("Deve permitir desvinculação se o solicitante for o próprio usuário")
    void devePermitirProprioUsuarioDesvincular() {
        UsuarioPet vinculoCoCuidador = UsuarioPet.builder()
                .id(new UsuarioPetId(2L, 10L))
                .usuario(Usuario.builder().id(2L).build())
                .pet(Pet.builder().id(10L).build())
                .responsavelPrincipal(false)
                .build();

        assertDoesNotThrow(() -> validator.validarPermissaoDesvinculacao(vinculoCoCuidador, 2L));
    }

    @Test
    @DisplayName("Deve permitir desvinculação se o solicitante for o responsável principal")
    void devePermitirResponsavelDesvincularCoCuidador() {
        UsuarioPet vinculoCoCuidador = UsuarioPet.builder()
                .id(new UsuarioPetId(2L, 10L))
                .usuario(Usuario.builder().id(2L).build())
                .pet(Pet.builder().id(10L).build())
                .responsavelPrincipal(false)
                .build();

        when(usuarioPetRepository.isResponsavelPrincipal(1L, 10L)).thenReturn(true);

        assertDoesNotThrow(() -> validator.validarPermissaoDesvinculacao(vinculoCoCuidador, 1L));
    }
}
