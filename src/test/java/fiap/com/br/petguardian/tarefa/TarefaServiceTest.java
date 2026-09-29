package fiap.com.br.petguardian.tarefa;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetRepository;
import fiap.com.br.petguardian.tarefa.dto.TarefaConclusaoRequest;
import fiap.com.br.petguardian.tarefa.dto.TarefaRequest;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.tarefa.status.Status;
import fiap.com.br.petguardian.tarefa.status.StatusService;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.validation.TarefaValidator;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private TarefaValidator tarefaValidator;

    @InjectMocks
    private TarefaService tarefaService;

    @Test
    @DisplayName("Deve listar tarefas com auto-expiracao")
    void deveListarTarefasComExpiracao() {
        Pageable pageable = PageRequest.of(0, 10);
        Status pendente = Status.builder().id(1L).nomeStatus(EnumStatus.PENDENTE).build();
        Status expirado = Status.builder().id(3L).nomeStatus(EnumStatus.EXPIRADO).build();

        when(statusService.findStatus(EnumStatus.PENDENTE)).thenReturn(pendente);
        when(statusService.findStatus(EnumStatus.EXPIRADO)).thenReturn(expirado);
        when(tarefaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<Tarefa> resultado = tarefaService.findAll(pageable);

        assertNotNull(resultado);
        verify(tarefaRepository).expirarTarefasPendentesAtrasadas(any(LocalDateTime.class), eq(pendente), eq(expirado));
    }

    @Test
    @DisplayName("Deve criar nova tarefa vinculada a um cuidador do pet com status PENDENTE")
    void deveCriarTarefa() {
        var request = new TarefaRequest("Remédio", 15, "Dar antibiótico", LocalDateTime.now().plusDays(1), 1L, 10L, "PENDENTE", null);

        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").build();
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
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(statusService.findStatus(EnumStatus.PENDENTE)).thenReturn(statusPendente);
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefaSalva);

        Tarefa resultado = tarefaService.create(request);

        assertNotNull(resultado);
        assertEquals("Remédio", resultado.getTitulo());
        assertEquals(EnumStatus.PENDENTE, resultado.getStatus().getNomeStatus());
        verify(tarefaValidator).validarCuidadorDoPet(1L, 10L);
        verify(tarefaRepository).save(any(Tarefa.class));
    }

    @Test
    @DisplayName("Deve concluir tarefa somando pontos e registrando concluinte e data")
    void deveConcluirTarefa() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Usuario criador = Usuario.builder().id(1L).nome("Enzo").build();
        Usuario concluinte = Usuario.builder().id(2L).nome("CoCuidador").build();

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
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(concluinte));
        when(statusService.findStatus(EnumStatus.CONCLUIDO)).thenReturn(statusConcluido);
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(inv -> inv.getArgument(0));

        var conclusaoReq = new TarefaConclusaoRequest(2L);
        Tarefa resultado = tarefaService.concluir(100L, conclusaoReq);

        assertNotNull(resultado);
        assertEquals(EnumStatus.CONCLUIDO, resultado.getStatus().getNomeStatus());
        assertEquals(2L, resultado.getUsuario().getId());
        assertNotNull(resultado.getConclusao());
        verify(tarefaValidator).validarPendenteParaConclusao(tarefa);
        verify(tarefaValidator).validarCuidadorDoPet(2L, 10L);
    }

    @Test
    @DisplayName("Deve desmarcar tarefa concluida voltando para PENDENTE e limpando conclusao")
    void deveDesmarcarTarefa() {
        Usuario cuidador = Usuario.builder().id(2L).build();
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

        when(statusService.findStatus(EnumStatus.PENDENTE)).thenReturn(statusPendente);
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(cuidador));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(inv -> inv.getArgument(0));

        Tarefa resultado = tarefaService.desmarcar(100L, 2L);

        assertNotNull(resultado);
        assertEquals(EnumStatus.PENDENTE, resultado.getStatus().getNomeStatus());
        assertNull(resultado.getConclusao());
        verify(tarefaValidator).validarCuidadorDoPet(2L, 10L);
        verify(tarefaValidator).validarConcluidaParaDesmarcar(tarefa);
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
    @DisplayName("Deve listar tarefas do cuidador por status")
    void deveListarTarefasPorStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Status pendente = Status.builder().id(1L).nomeStatus(EnumStatus.PENDENTE).build();
        Status expirado = Status.builder().id(3L).nomeStatus(EnumStatus.EXPIRADO).build();

        when(statusService.findStatus(EnumStatus.PENDENTE)).thenReturn(pendente);
        when(statusService.findStatus(EnumStatus.EXPIRADO)).thenReturn(expirado);
        when(tarefaRepository.findAllDoCuidadorByStatus(1L, EnumStatus.PENDENTE, pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<Tarefa> resultado = tarefaService.findAllByUsuario(1L, "PENDENTE", pageable);

        assertNotNull(resultado);
        verify(tarefaRepository).findAllDoCuidadorByStatus(1L, EnumStatus.PENDENTE, pageable);
    }
}
