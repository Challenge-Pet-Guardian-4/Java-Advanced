package fiap.com.br.petguardian.tarefa;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetRepository;
import fiap.com.br.petguardian.tarefa.dto.TarefaRequest;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.tarefa.status.Status;
import fiap.com.br.petguardian.tarefa.status.StatusService;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private StatusService statusService;

    @Mock
    private UsuarioPetRepository usuarioPetRepository;

    @InjectMocks
    private TarefaService tarefaService;

    @BeforeEach
    void setUp() {
        Status statusPendente = Status.builder().id(1L).nomeStatus(EnumStatus.PENDENTE).build();
        Status statusExpirado = Status.builder().id(3L).nomeStatus(EnumStatus.EXPIRADO).build();
        lenient().when(statusService.findStatus(EnumStatus.PENDENTE)).thenReturn(statusPendente);
        lenient().when(statusService.findStatus(EnumStatus.EXPIRADO)).thenReturn(statusExpirado);
    }

    @Test
    @DisplayName("Deve listar tarefas executando rotina de expiracao no banco")
    void deveListarTarefasExecutandoExpiracao() {
        Pageable pageable = PageRequest.of(0, 10);
        when(tarefaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<Tarefa> resultado = tarefaService.findAll(pageable);

        assertNotNull(resultado);
        verify(tarefaRepository).findAll(pageable);
        verify(tarefaRepository).expirarTarefasPendentesAtrasadas(any(), any(), any());
    }

    @Test
    @DisplayName("Deve criar nova tarefa vinculada ao cuidador autenticado com status PENDENTE")
    void deveCriarTarefa() {
        var request = new TarefaRequest("Remédio", 15, "Dar antibiótico", LocalDateTime.now().plusDays(1), 10L, "PENDENTE", null);

        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").build();
        Status statusPendente = Status.builder().id(1L).nomeStatus(EnumStatus.PENDENTE).build();
        Tarefa tarefaSalva = Tarefa.builder()
                .id(100L)
                .titulo("Remédio")
                .pontosTarefa(15)
                .descricao("Dar antibiótico")
                .criacao(LocalDateTime.now())
                .prazo(LocalDateTime.now().plusDays(1))
                .status(statusPendente)
                .usuario(usuario)
                .pet(pet)
                .build();

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("enzo@fiap.com.br", 10L)).thenReturn(true);
        when(usuarioRepository.findByEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(statusService.findStatus(EnumStatus.PENDENTE)).thenReturn(statusPendente);
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefaSalva);

        Tarefa resultado = tarefaService.create(request, "enzo@fiap.com.br");

        assertNotNull(resultado);
        assertEquals("Remédio", resultado.getTitulo());
        assertEquals(EnumStatus.PENDENTE, resultado.getStatus().getNomeStatus());
        verify(tarefaRepository).save(any(Tarefa.class));
    }

    @Test
    @DisplayName("Deve lancar excecao ao criar tarefa quando usuario nao for cuidador do pet")
    void deveLancarExcecaoAoCriarTarefaQuandoNaoForCuidador() {
        var request = new TarefaRequest("Remédio", 15, "Dar antibiótico", LocalDateTime.now().plusDays(1), 10L, "PENDENTE", null);
        Pet pet = Pet.builder().id(10L).nome("Thor").build();

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("outro@fiap.com.br", 10L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> tarefaService.create(request, "outro@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve concluir tarefa somando pontos e registrando concluinte e data")
    void deveConcluirTarefa() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario criador = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").build();
        Usuario concluinte = Usuario.builder().id(2L).nome("CoCuidador").email("concluinte@fiap.com.br").build();

        Status statusPendente = Status.builder().id(1L).nomeStatus(EnumStatus.PENDENTE).build();
        Status statusConcluido = Status.builder().id(2L).nomeStatus(EnumStatus.CONCLUIDO).build();

        Tarefa tarefa = Tarefa.builder()
                .id(100L)
                .titulo("Passeio")
                .pontosTarefa(20)
                .descricao("Passeio no parque")
                .criacao(LocalDateTime.now().minusHours(2))
                .prazo(LocalDateTime.now().plusHours(2))
                .status(statusPendente)
                .usuario(criador)
                .pet(pet)
                .build();

        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("concluinte@fiap.com.br", 10L)).thenReturn(true);
        when(usuarioRepository.findByEmailIgnoreCase("concluinte@fiap.com.br")).thenReturn(Optional.of(concluinte));
        when(statusService.findStatus(EnumStatus.CONCLUIDO)).thenReturn(statusConcluido);
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(inv -> inv.getArgument(0));

        Tarefa resultado = tarefaService.concluir(100L, "concluinte@fiap.com.br");

        assertNotNull(resultado);
        assertEquals(EnumStatus.CONCLUIDO, resultado.getStatus().getNomeStatus());
        assertEquals(2L, resultado.getUsuario().getId());
        assertNotNull(resultado.getConclusao());
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar concluir tarefa nao pendente")
    void deveLancarExcecaoAoConcluirTarefaNaoPendente() {
        Status statusConcluido = Status.builder().id(2L).nomeStatus(EnumStatus.CONCLUIDO).build();
        Tarefa tarefa = Tarefa.builder().id(100L).status(statusConcluido).build();

        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));

        assertThrows(IllegalArgumentException.class, () -> tarefaService.concluir(100L, "cuidador@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve desmarcar tarefa concluida voltando para PENDENTE e limpando conclusao")
    void deveDesmarcarTarefa() {
        Usuario cuidador = Usuario.builder().id(2L).email("cuidador@fiap.com.br").build();
        Pet pet = Pet.builder().id(10L).build();
        Status statusPendente = Status.builder().id(1L).nomeStatus(EnumStatus.PENDENTE).build();
        Status statusConcluido = Status.builder().id(2L).nomeStatus(EnumStatus.CONCLUIDO).build();

        Tarefa tarefa = Tarefa.builder()
                .id(100L)
                .titulo("Passeio")
                .pontosTarefa(20)
                .descricao("Passeio no parque")
                .status(statusConcluido)
                .conclusao(LocalDateTime.now())
                .usuario(cuidador)
                .pet(pet)
                .build();

        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("cuidador@fiap.com.br", 10L)).thenReturn(true);
        when(statusService.findStatus(EnumStatus.PENDENTE)).thenReturn(statusPendente);
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(inv -> inv.getArgument(0));

        Tarefa resultado = tarefaService.desmarcar(100L, "cuidador@fiap.com.br");

        assertNotNull(resultado);
        assertEquals(EnumStatus.PENDENTE, resultado.getStatus().getNomeStatus());
        assertNull(resultado.getConclusao());
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar desmarcar tarefa que nao esteja concluida")
    void deveLancarExcecaoAoDesmarcarTarefaNaoConcluida() {
        Pet pet = Pet.builder().id(10L).build();
        Status statusPendente = Status.builder().id(1L).nomeStatus(EnumStatus.PENDENTE).build();
        Tarefa tarefa = Tarefa.builder().id(100L).pet(pet).status(statusPendente).build();

        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("cuidador@fiap.com.br", 10L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> tarefaService.desmarcar(100L, "cuidador@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve consultar pontos totais acumulados pelo usuario")
    void deveConsultarPontosUsuario() {
        Usuario usuario = Usuario.builder().id(1L).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(tarefaRepository.calcularPontosTotaisUsuario(1L, EnumStatus.CONCLUIDO)).thenReturn(120);

        Integer pontos = tarefaService.calcularPontosTotaisUsuario(1L);

        assertEquals(120, pontos);
    }

    @Test
    @DisplayName("Deve deletar tarefa com sucesso chamando deleteById")
    void deveDeletarTarefa() {
        Tarefa tarefa = Tarefa.builder().id(50L).build();
        when(tarefaRepository.findById(50L)).thenReturn(Optional.of(tarefa));

        tarefaService.delete(50L);

        verify(tarefaRepository).deleteById(50L);
    }

    @Test
    @DisplayName("Deve listar tarefas do cuidador por email e status")
    void deveListarTarefasPorEmail() {
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").build();
        Pageable pageable = PageRequest.of(0, 10);

        when(usuarioRepository.findByEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(tarefaRepository.findAllByPetUsuarioPetsUsuarioIdAndStatusNomeStatus(1L, EnumStatus.PENDENTE, pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<Tarefa> resultado = tarefaService.findAllByEmail("enzo@fiap.com.br", "PENDENTE", pageable);

        assertNotNull(resultado);
        verify(tarefaRepository).findAllByPetUsuarioPetsUsuarioIdAndStatusNomeStatus(1L, EnumStatus.PENDENTE, pageable);
    }

    @Test
    @DisplayName("Deve consultar pontos totais acumulados pelo email do usuario")
    void deveConsultarPontosPorEmail() {
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").build();
        when(usuarioRepository.findByEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(tarefaRepository.calcularPontosTotaisUsuario(1L, EnumStatus.CONCLUIDO)).thenReturn(150);

        Integer pontos = tarefaService.calcularPontosTotaisEmail("enzo@fiap.com.br");

        assertEquals(150, pontos);
    }

    @Test
    @DisplayName("Deve verificar se usuario e cuidador da tarefa")
    void deveVerificarCuidadorDaTarefa() {
        Pet pet = Pet.builder().id(10L).build();
        Tarefa tarefa = Tarefa.builder().id(100L).pet(pet).build();

        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("enzo@fiap.com.br", 10L)).thenReturn(true);

        assertTrue(tarefaService.isCuidadorDaTarefa(100L, "enzo@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve listar todas as tarefas do cuidador quando status for ALL")
    void deveListarTodasTarefasPorEmailQuandoStatusAll() {
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").build();
        Pageable pageable = PageRequest.of(0, 10);

        when(usuarioRepository.findByEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(tarefaRepository.findAllByPetUsuarioPetsUsuarioId(1L, pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<Tarefa> resultado = tarefaService.findAllByEmail("enzo@fiap.com.br", "ALL", pageable);

        assertNotNull(resultado);
        verify(tarefaRepository).findAllByPetUsuarioPetsUsuarioId(1L, pageable);
    }
}
