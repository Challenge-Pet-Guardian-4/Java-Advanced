package fiap.com.br.petguardian.usuariopet;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetRepository;
import fiap.com.br.petguardian.tarefa.TarefaRepository;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorRequest;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorResponse;
import fiap.com.br.petguardian.usuariopet.dto.TransferirResponsabilidadeRequest;
import fiap.com.br.petguardian.validation.UsuarioPetValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioPetServiceTest {

    @Mock
    private UsuarioPetRepository usuarioPetRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private UsuarioPetValidator usuarioPetValidator;

    @Mock
    private RedeCuidadoMapper redeCuidadoMapper;

    @InjectMocks
    private UsuarioPetService usuarioPetService;

    @Test
    @DisplayName("Deve convidar co-cuidador com sucesso")
    void deveConvidarCoCuidador() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Usuario convidado = Usuario.builder().id(2L).nome("Familiar").email("familiar@fiap.com.br").build();
        UsuarioPet novoVinculo = UsuarioPet.builder()
                .id(new UsuarioPetId(2L, 10L))
                .usuario(convidado)
                .pet(pet)
                .responsavelPrincipal(false)
                .build();

        var request = new CoCuidadorRequest(1L, "familiar@fiap.com.br");

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(usuarioRepository.findByEmailIgnoreCase("familiar@fiap.com.br")).thenReturn(Optional.of(convidado));
        when(usuarioPetRepository.save(any(UsuarioPet.class))).thenReturn(novoVinculo);

        CoCuidadorResponse response = usuarioPetService.convidarCoCuidador(10L, request);

        assertNotNull(response);
        assertEquals(2L, response.usuarioId());
        assertEquals("Familiar", response.nome());
        assertFalse(response.responsavelPrincipal());
        verify(usuarioPetValidator).validarResponsavelPrincipal(1L, 10L);
        verify(usuarioPetValidator).validarUsuarioNaoVinculado(2L, 10L);
    }

    @Test
    @DisplayName("Deve desvincular co-cuidador com sucesso")
    void deveDesvincularCuidador() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario usuario = Usuario.builder().id(2L).build();
        UsuarioPet vinculo = UsuarioPet.builder().id(new UsuarioPetId(2L, 10L)).usuario(usuario).pet(pet).responsavelPrincipal(false).build();

        when(usuarioPetRepository.findByUsuarioIdAndPetId(2L, 10L)).thenReturn(Optional.of(vinculo));

        usuarioPetService.desvincularCuidador(10L, 2L, 1L);

        verify(usuarioPetValidator).validarPermissaoDesvinculacao(vinculo, 1L);
        verify(usuarioPetRepository).delete(vinculo);
    }

    @Test
    @DisplayName("Deve transferir titularidade de responsavel principal")
    void deveTransferirResponsabilidadePrincipal() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario novoResp = Usuario.builder().id(2L).build();
        UsuarioPet vinculoNovoResp = UsuarioPet.builder().id(new UsuarioPetId(2L, 10L)).usuario(novoResp).pet(pet).responsavelPrincipal(false).build();

        var request = new TransferirResponsabilidadeRequest(1L, 2L);

        when(usuarioPetRepository.findByUsuarioIdAndPetId(2L, 10L)).thenReturn(Optional.of(vinculoNovoResp));
        when(usuarioPetRepository.save(vinculoNovoResp)).thenReturn(vinculoNovoResp);

        usuarioPetService.transferirResponsabilidadePrincipal(10L, request);

        verify(usuarioPetValidator).validarResponsavelPrincipal(1L, 10L);
        verify(usuarioPetRepository).limparResponsavelPrincipalPorPet(10L);
        assertTrue(vinculoNovoResp.isResponsavelPrincipal());
        verify(usuarioPetRepository).save(vinculoNovoResp);
    }

    @Test
    @DisplayName("Deve listar todos os cuidadores de um pet")
    void deveListarCuidadoresDoPet() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Usuario tutor = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").build();
        UsuarioPet vinculo = UsuarioPet.builder().id(new UsuarioPetId(1L, 10L)).usuario(tutor).pet(pet).responsavelPrincipal(true).build();

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(usuarioPetRepository.findAllByPetId(10L)).thenReturn(List.of(vinculo));

        List<CoCuidadorResponse> cuidadores = usuarioPetService.listarCuidadoresDoPet(10L);

        assertNotNull(cuidadores);
        assertEquals(1, cuidadores.size());
        assertEquals("Enzo", cuidadores.get(0).nome());
        assertTrue(cuidadores.get(0).responsavelPrincipal());
    }
}
