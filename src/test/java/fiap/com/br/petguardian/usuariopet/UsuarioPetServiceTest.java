package fiap.com.br.petguardian.usuariopet;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetService;
import fiap.com.br.petguardian.pet.raca.Raca;
import fiap.com.br.petguardian.tarefa.TarefaRepository;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioService;
import fiap.com.br.petguardian.usuariopet.dto.RedeCuidadoResponse;
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
import java.util.Map;
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
    private UsuarioService usuarioService;

    @Mock
    private PetService petService;

    @Mock
    private TarefaRepository tarefaRepository;

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

        when(petService.findById(10L)).thenReturn(pet);
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("familiar@fiap.com.br", 10L)).thenReturn(false);
        when(usuarioService.findByEmail("familiar@fiap.com.br")).thenReturn(convidado);
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

        when(petService.findById(10L)).thenReturn(pet);
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("familiar@fiap.com.br", 10L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> usuarioPetService.convidarCoCuidador(10L, request));
    }

    @Test
    @DisplayName("Deve desvincular co-cuidador com sucesso via email")
    void deveDesvincularCuidador() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario cuidador = Usuario.builder().id(2L).email("cuidador@fiap.com.br").build();
        UsuarioPet vinculo = UsuarioPet.builder().id(new UsuarioPetId(2L, 10L)).usuario(cuidador).pet(pet).responsavelPrincipal(false).build();

        when(usuarioPetRepository.findByUsuarioEmailIgnoreCaseAndPetId("cuidador@fiap.com.br", 10L)).thenReturn(Optional.of(vinculo));

        usuarioPetService.desvincularCuidador(10L, "cuidador@fiap.com.br");

        verify(usuarioPetRepository).delete(vinculo);
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar desvincular o responsavel principal sem transferir titularidade")
    void deveLancarExcecaoAoDesvincularResponsavelPrincipal() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario principal = Usuario.builder().id(1L).email("principal@fiap.com.br").build();
        UsuarioPet vinculo = UsuarioPet.builder().id(new UsuarioPetId(1L, 10L)).usuario(principal).pet(pet).responsavelPrincipal(true).build();

        when(usuarioPetRepository.findByUsuarioEmailIgnoreCaseAndPetId("principal@fiap.com.br", 10L)).thenReturn(Optional.of(vinculo));

        assertThrows(IllegalArgumentException.class, () -> usuarioPetService.desvincularCuidador(10L, "principal@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve transferir titularidade de responsavel principal")
    void deveTransferirResponsabilidadePrincipal() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario novoResp = Usuario.builder().id(2L).email("novo@fiap.com.br").build();
        UsuarioPet vinculoNovoResp = UsuarioPet.builder().id(new UsuarioPetId(2L, 10L)).usuario(novoResp).pet(pet).responsavelPrincipal(false).build();

        var request = new TransferirResponsabilidadeRequest("novo@fiap.com.br");

        when(usuarioPetRepository.findByUsuarioEmailIgnoreCaseAndPetId("novo@fiap.com.br", 10L)).thenReturn(Optional.of(vinculoNovoResp));

        usuarioPetService.transferirResponsabilidadePrincipal(10L, request);

        verify(usuarioPetRepository).transferirResponsavelPrincipalNoBanco(10L, 2L);
    }

    @Test
    @DisplayName("Deve lancar excecao ao transferir para quem ja e responsavel principal")
    void deveLancarExcecaoAoTransferirParaQuemJaEResponsavelPrincipal() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario novoResp = Usuario.builder().id(2L).email("novo@fiap.com.br").build();
        UsuarioPet vinculoNovoResp = UsuarioPet.builder().id(new UsuarioPetId(2L, 10L)).usuario(novoResp).pet(pet).responsavelPrincipal(true).build();

        var request = new TransferirResponsabilidadeRequest("novo@fiap.com.br");

        when(usuarioPetRepository.findByUsuarioEmailIgnoreCaseAndPetId("novo@fiap.com.br", 10L)).thenReturn(Optional.of(vinculoNovoResp));

        assertThrows(IllegalArgumentException.class, () -> usuarioPetService.transferirResponsabilidadePrincipal(10L, request));
    }

    @Test
    @DisplayName("Deve listar todos os cuidadores de um pet")
    void deveListarCuidadoresDoPet() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Usuario tutor = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").build();
        UsuarioPet vinculo = UsuarioPet.builder().id(new UsuarioPetId(1L, 10L)).usuario(tutor).pet(pet).responsavelPrincipal(true).build();

        when(petService.findById(10L)).thenReturn(pet);
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

        when(usuarioService.findById(1L)).thenReturn(usuario);
        when(usuarioPetRepository.findAllByUsuarioEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(List.of());

        RedeCuidadoResponse resultado = usuarioPetService.montarRedeCuidado(1L);

        assertNotNull(resultado);
        assertEquals("enzo@fiap.com.br", resultado.emailUsuario());
        assertEquals("Enzo", resultado.nomeUsuario());
        assertEquals(0, resultado.pets().size());
        assertEquals(0, resultado.coCuidadores().size());
        assertEquals(0, resultado.totalTarefasPendentes());
        assertEquals(0, resultado.totalTarefasConcluidas());
        assertEquals(0, resultado.pontosAcumulados());
        assertEquals("BRONZE", resultado.nivelGamificacao());
    }

    @Test
    @DisplayName("Deve montar rede de cuidado a partir do email do usuario")
    void deveMontarRedeDeCuidadoPorEmail() {
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").build();

        when(usuarioService.findByEmail("enzo@fiap.com.br")).thenReturn(usuario);
        when(usuarioPetRepository.findAllByUsuarioEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(List.of());

        RedeCuidadoResponse resultado = usuarioPetService.montarRedeCuidado("enzo@fiap.com.br");

        assertNotNull(resultado);
        assertEquals("enzo@fiap.com.br", resultado.emailUsuario());
        assertEquals("BRONZE", resultado.nivelGamificacao());
    }

    @Test
    @DisplayName("Deve montar rede de cuidado com vinculos utilizando Stored Procedure Oracle")
    void deveMontarRedeDeCuidadoComVinculosEProcedureOracle() {
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").build();
        Raca raca = Raca.builder().id(1L).nome("Golden Retriever").build();
        Pet pet = Pet.builder().id(10L).nome("Thor").raca(raca).build();
        UsuarioPet vinculo = UsuarioPet.builder()
                .id(new UsuarioPetId(1L, 10L))
                .usuario(usuario)
                .pet(pet)
                .responsavelPrincipal(true)
                .build();

        Map<String, Object> resumoDb = Map.of(
                "p_tarefas_pendentes", 3,
                "p_tarefas_concluidas", 7,
                "p_pontos_acumulados", 150
        );

        when(usuarioService.findByEmail("enzo@fiap.com.br")).thenReturn(usuario);
        when(usuarioPetRepository.findAllByUsuarioEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(List.of(vinculo));
        when(tarefaRepository.countTarefasByPetIdIn(List.of(10L))).thenReturn(List.<Object[]>of(new Object[]{10L, 1L}));
        when(usuarioPetRepository.findAllByPetIdIn(List.of(10L))).thenReturn(List.of(vinculo));
        when(usuarioPetRepository.obterResumoRedeCuidadoNoBanco(1L)).thenReturn(resumoDb);
        when(usuarioPetRepository.obterNivelUsuarioNoBanco(1L)).thenReturn("PRATA");

        RedeCuidadoResponse resultado = usuarioPetService.montarRedeCuidado("enzo@fiap.com.br");

        assertNotNull(resultado);
        assertEquals("enzo@fiap.com.br", resultado.emailUsuario());
        assertEquals("Enzo", resultado.nomeUsuario());
        assertEquals(1, resultado.pets().size());
        assertEquals("Thor", resultado.pets().get(0).nome());
        assertEquals("Golden Retriever", resultado.pets().get(0).raca());
        assertTrue(resultado.pets().get(0).responsavelPrincipal());
        assertEquals(1, resultado.pets().get(0).totalTarefas());
        assertEquals(0, resultado.coCuidadores().size()); // Sem co-cuidadores alem do proprio usuario logado
        assertEquals(3, resultado.totalTarefasPendentes());
        assertEquals(7, resultado.totalTarefasConcluidas());
        assertEquals(150, resultado.pontosAcumulados());
        assertEquals("PRATA", resultado.nivelGamificacao());

        verify(usuarioPetRepository).obterResumoRedeCuidadoNoBanco(1L);
        verify(usuarioPetRepository).obterNivelUsuarioNoBanco(1L);
    }

    @Test
    @DisplayName("Deve verificar se usuario e responsavel principal")
    void deveVerificarResponsavelPrincipal() {
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetIdAndResponsavelPrincipalTrue("enzo@fiap.com.br", 10L)).thenReturn(true);
        assertTrue(usuarioPetService.isResponsavelPrincipal(10L, "enzo@fiap.com.br"));

        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetIdAndResponsavelPrincipalTrue("outro@fiap.com.br", 10L)).thenReturn(false);
        assertFalse(usuarioPetService.isResponsavelPrincipal(10L, "outro@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve verificar se usuario e cuidador do pet")
    void deveVerificarCuidadorDoPet() {
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("enzo@fiap.com.br", 10L)).thenReturn(true);
        assertTrue(usuarioPetService.isCuidadorDoPet(10L, "enzo@fiap.com.br"));

        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("outro@fiap.com.br", 10L)).thenReturn(false);
        assertFalse(usuarioPetService.isCuidadorDoPet(10L, "outro@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve montar rede de cuidado por usuario ID")
    void deveMontarRedeDeCuidadoPorUsuarioId() {
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").build();
        when(usuarioService.findById(1L)).thenReturn(usuario);
        when(usuarioPetRepository.findAllByUsuarioEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(List.of());

        RedeCuidadoResponse resultado = usuarioPetService.montarRedeCuidado(1L);

        assertNotNull(resultado);
        assertEquals("enzo@fiap.com.br", resultado.emailUsuario());
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao nao encontrar usuario por id")
    void deveLancarExceptionAoNaoEncontrarUsuarioPorId() {
        when(usuarioService.findById(99L)).thenThrow(new ResourceNotFoundException("Usuário com id 99 não encontrado."));
        assertThrows(ResourceNotFoundException.class, () -> usuarioPetService.montarRedeCuidado(99L));
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao nao encontrar pet por id")
    void deveLancarExceptionAoNaoEncontrarPetPorId() {
        when(petService.findById(99L)).thenThrow(new ResourceNotFoundException("Pet com id 99 não encontrado."));
        assertThrows(ResourceNotFoundException.class, () -> usuarioPetService.listarCuidadoresDoPet(99L));
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao desvincular cuidador inexistente")
    void deveLancarExceptionAoDesvincularCuidadorInexistente() {
        when(usuarioPetRepository.findByUsuarioEmailIgnoreCaseAndPetId("naoexiste@fiap.com.br", 10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> usuarioPetService.desvincularCuidador(10L, "naoexiste@fiap.com.br"));
    }


    @Test
    @DisplayName("Deve montar rede de cuidado com multiplos co-cuidadores")
    void deveMontarRedeCuidadoComMultiplosCoCuidadores() {
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").nome("Enzo").build();
        Usuario maria = Usuario.builder().id(2L).email("maria@fiap.com.br").nome("Maria").build();
        Raca raca = Raca.builder().id(1L).nome("Golden Retriever").build();
        Pet pet1 = Pet.builder().id(10L).nome("Thor").raca(raca).build();
        Pet pet2 = Pet.builder().id(11L).nome("Mel").raca(raca).build();

        UsuarioPet vEnzo = UsuarioPet.builder().id(new UsuarioPetId(1L, 10L)).usuario(usuario).pet(pet1).responsavelPrincipal(true).build();
        UsuarioPet vMariaThor = UsuarioPet.builder().id(new UsuarioPetId(2L, 10L)).usuario(maria).pet(pet1).responsavelPrincipal(false).build();
        UsuarioPet vMariaMel = UsuarioPet.builder().id(new UsuarioPetId(2L, 11L)).usuario(maria).pet(pet2).responsavelPrincipal(true).build();

        when(usuarioService.findByEmail("enzo@fiap.com.br")).thenReturn(usuario);
        when(usuarioPetRepository.findAllByUsuarioEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(List.of(vEnzo));
        when(tarefaRepository.countTarefasByPetIdIn(List.of(10L))).thenReturn(List.<Object[]>of(new Object[]{10L, 2L}));
        when(usuarioPetRepository.findAllByPetIdIn(List.of(10L))).thenReturn(List.of(vEnzo, vMariaThor, vMariaMel));
        when(usuarioPetRepository.obterResumoRedeCuidadoNoBanco(1L)).thenReturn(Map.of("p_tarefas_pendentes", 1, "p_tarefas_concluidas", 4, "p_pontos_acumulados", 100));
        when(usuarioPetRepository.obterNivelUsuarioNoBanco(1L)).thenReturn("OURO");

        RedeCuidadoResponse resultado = usuarioPetService.montarRedeCuidado("enzo@fiap.com.br");

        assertNotNull(resultado);
        assertEquals(1, resultado.coCuidadores().size());
        var mariaResumo = resultado.coCuidadores().get(0);
        assertEquals("Maria", mariaResumo.nome());
        assertEquals("maria@fiap.com.br", mariaResumo.email());
        assertTrue(mariaResumo.responsavelPrincipal());
        assertEquals(2, mariaResumo.petIds().size());
        assertEquals(List.of("Mel"), mariaResumo.petsPrincipalNomes());
        assertEquals(List.of("Thor"), mariaResumo.petsAjudaNomes());
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao montar rede de cuidado com email inexistente")
    void deveLancarExceptionAoMontarRedeCuidadoQuandoEmailNaoExistir() {
        when(usuarioService.findByEmail("inexistente@fiap.com.br")).thenThrow(new ResourceNotFoundException("Usuário com e-mail inexistente@fiap.com.br não encontrado."));
        assertThrows(ResourceNotFoundException.class, () -> usuarioPetService.montarRedeCuidado("inexistente@fiap.com.br"));
    }

}
