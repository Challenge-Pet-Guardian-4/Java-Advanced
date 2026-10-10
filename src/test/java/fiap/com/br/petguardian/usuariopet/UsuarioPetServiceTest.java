package fiap.com.br.petguardian.usuariopet;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetRepository;
import fiap.com.br.petguardian.tarefa.TarefaRepository;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.usuario.dto.RedeCuidadoResponse;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorRequest;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorResponse;
import fiap.com.br.petguardian.usuariopet.dto.TransferirResponsabilidadeRequest;
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

        var request = new CoCuidadorRequest("familiar@fiap.com.br");

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("familiar@fiap.com.br", 10L)).thenReturn(false);
        when(usuarioRepository.findByEmailIgnoreCase("familiar@fiap.com.br")).thenReturn(Optional.of(convidado));
        when(usuarioPetRepository.save(any(UsuarioPet.class))).thenReturn(novoVinculo);

        CoCuidadorResponse response = usuarioPetService.convidarCoCuidador(10L, request);

        assertNotNull(response);
        assertEquals("Familiar", response.nome());
        assertEquals("familiar@fiap.com.br", response.email());
        assertFalse(response.responsavelPrincipal());
        verify(usuarioPetRepository).save(any(UsuarioPet.class));
    }

    @Test
    @DisplayName("Deve lancar excecao ao convidar usuario ja vinculado ao pet")
    void deveLancarExcecaoAoConvidarUsuarioJaVinculado() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        var request = new CoCuidadorRequest("familiar@fiap.com.br");

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("familiar@fiap.com.br", 10L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> usuarioPetService.convidarCoCuidador(10L, request));
    }

    @Test
    @DisplayName("Deve desvincular co-cuidador com sucesso via email pelo responsavel principal")
    void deveDesvincularCuidador() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario cuidador = Usuario.builder().id(2L).email("cuidador@fiap.com.br").build();
        UsuarioPet vinculo = UsuarioPet.builder().id(new UsuarioPetId(2L, 10L)).usuario(cuidador).pet(pet).responsavelPrincipal(false).build();

        when(usuarioPetRepository.findByUsuarioEmailIgnoreCaseAndPetId("cuidador@fiap.com.br", 10L)).thenReturn(Optional.of(vinculo));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetIdAndResponsavelPrincipalTrue("enzo@fiap.com.br", 10L)).thenReturn(true);

        usuarioPetService.desvincularCuidador(10L, "cuidador@fiap.com.br", "enzo@fiap.com.br");

        verify(usuarioPetRepository).delete(vinculo);
    }

    @Test
    @DisplayName("Deve permitir o proprio co-cuidador se desvincular")
    void devePermitirProprioCuidadorDesvincular() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario cuidador = Usuario.builder().id(2L).email("cuidador@fiap.com.br").build();
        UsuarioPet vinculo = UsuarioPet.builder().id(new UsuarioPetId(2L, 10L)).usuario(cuidador).pet(pet).responsavelPrincipal(false).build();

        when(usuarioPetRepository.findByUsuarioEmailIgnoreCaseAndPetId("cuidador@fiap.com.br", 10L)).thenReturn(Optional.of(vinculo));

        usuarioPetService.desvincularCuidador(10L, "cuidador@fiap.com.br", "cuidador@fiap.com.br");

        verify(usuarioPetRepository).delete(vinculo);
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar desvincular o responsavel principal sem transferir titularidade")
    void deveLancarExcecaoAoDesvincularResponsavelPrincipal() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario principal = Usuario.builder().id(1L).email("principal@fiap.com.br").build();
        UsuarioPet vinculo = UsuarioPet.builder().id(new UsuarioPetId(1L, 10L)).usuario(principal).pet(pet).responsavelPrincipal(true).build();

        when(usuarioPetRepository.findByUsuarioEmailIgnoreCaseAndPetId("principal@fiap.com.br", 10L)).thenReturn(Optional.of(vinculo));

        assertThrows(IllegalArgumentException.class, () -> usuarioPetService.desvincularCuidador(10L, "principal@fiap.com.br", "principal@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar desvincular cuidador sem ser o proprio ou responsavel principal")
    void deveLancarExcecaoAoDesvincularCuidadorSemPermissao() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario cuidador = Usuario.builder().id(2L).email("cuidador@fiap.com.br").build();
        UsuarioPet vinculo = UsuarioPet.builder().id(new UsuarioPetId(2L, 10L)).usuario(cuidador).pet(pet).responsavelPrincipal(false).build();

        when(usuarioPetRepository.findByUsuarioEmailIgnoreCaseAndPetId("cuidador@fiap.com.br", 10L)).thenReturn(Optional.of(vinculo));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetIdAndResponsavelPrincipalTrue("estranho@fiap.com.br", 10L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> usuarioPetService.desvincularCuidador(10L, "cuidador@fiap.com.br", "estranho@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve transferir titularidade de responsavel principal")
    void deveTransferirResponsabilidadePrincipal() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario novoResp = Usuario.builder().id(2L).email("novo@fiap.com.br").build();
        UsuarioPet vinculoNovoResp = UsuarioPet.builder().id(new UsuarioPetId(2L, 10L)).usuario(novoResp).pet(pet).responsavelPrincipal(false).build();

        var request = new TransferirResponsabilidadeRequest("novo@fiap.com.br");

        when(usuarioPetRepository.findByUsuarioEmailIgnoreCaseAndPetId("novo@fiap.com.br", 10L)).thenReturn(Optional.of(vinculoNovoResp));

        usuarioPetService.transferirResponsabilidadePrincipal(10L, request, "enzo@fiap.com.br");

        verify(usuarioPetRepository).transferirResponsavelPrincipalNoBanco(10L, 2L);
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

    @Test
    @DisplayName("Deve montar rede de cuidado vazia quando usuario nao possui vinculos")
    void deveMontarRedeDeCuidadoVazia() {
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").build();
        var emptyResponse = new RedeCuidadoResponse("enzo@fiap.com.br", "Enzo", List.of(), List.of(), 0, 0, 0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(usuarioPetRepository.findAllByUsuarioEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(List.of());
        when(redeCuidadoMapper.toEmptyResponse(usuario)).thenReturn(emptyResponse);

        RedeCuidadoResponse resultado = usuarioPetService.montarRedeCuidado(1L);

        assertNotNull(resultado);
        assertEquals("enzo@fiap.com.br", resultado.emailUsuario());
        assertEquals("Enzo", resultado.nomeUsuario());
    }

    @Test
    @DisplayName("Deve montar rede de cuidado a partir do email do usuario")
    void deveMontarRedeDeCuidadoPorEmail() {
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").build();
        var emptyResponse = new RedeCuidadoResponse("enzo@fiap.com.br", "Enzo", List.of(), List.of(), 0, 0, 0);

        when(usuarioRepository.findByEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(usuarioPetRepository.findAllByUsuarioEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(List.of());
        when(redeCuidadoMapper.toEmptyResponse(usuario)).thenReturn(emptyResponse);

        RedeCuidadoResponse resultado = usuarioPetService.montarRedeCuidado("enzo@fiap.com.br");

        assertNotNull(resultado);
        assertEquals("enzo@fiap.com.br", resultado.emailUsuario());
    }
}
