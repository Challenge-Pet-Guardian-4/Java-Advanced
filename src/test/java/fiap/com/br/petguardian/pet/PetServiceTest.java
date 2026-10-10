package fiap.com.br.petguardian.pet;

import fiap.com.br.petguardian.pet.dto.PetHistoryResponse;
import fiap.com.br.petguardian.pet.dto.PetPontuacaoResponse;
import fiap.com.br.petguardian.pet.dto.PetRequest;
import fiap.com.br.petguardian.pet.raca.Raca;
import fiap.com.br.petguardian.pet.raca.RacaRepository;
import fiap.com.br.petguardian.tarefa.Tarefa;
import fiap.com.br.petguardian.tarefa.TarefaRepository;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.tarefa.status.Status;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.usuariopet.UsuarioPet;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
import fiap.com.br.petguardian.pet.dto.PetPontuacaoAgregadaResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioPetRepository usuarioPetRepository;

    @Mock
    private RacaRepository racaRepository;

    @Mock
    private TarefaRepository tarefaRepository;

    @InjectMocks
    private PetService petService;

    @Test
    @DisplayName("Deve listar todos os pets paginados")
    void deveListarTodosPets() {
        Pageable pageable = PageRequest.of(0, 10);
        Raca raca = Raca.builder().id(1L).nome("Golden Retriever").build();
        Pet pet = Pet.builder().id(1L).nome("Thor").raca(raca).dataNasc(LocalDate.now().minusYears(2)).porte(PetPorte.GRANDE).build();

        when(petRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(pet)));

        Page<Pet> resultado = petService.findAll(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Thor", resultado.getContent().get(0).getNome());
    }

    @Test
    @DisplayName("Deve criar um pet e vincular criador autenticado como responsavel principal")
    void deveCriarPetComSucesso() {
        var request = new PetRequest("Thor", LocalDate.now().minusYears(2), "Golden Retriever", "GRANDE", 'M', true);

        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").build();
        Raca raca = Raca.builder().id(1L).nome("Golden Retriever").build();
        Pet petSalvo = Pet.builder().id(10L).nome("Thor").raca(raca).dataNasc(LocalDate.now().minusYears(2)).porte(PetPorte.GRANDE).sexo('M').castrado(true).build();

        when(usuarioRepository.findByEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(racaRepository.findByNomeIgnoreCase("Golden Retriever")).thenReturn(Optional.of(raca));
        when(petRepository.save(any(Pet.class))).thenReturn(petSalvo);

        Pet resultado = petService.create(request, "enzo@fiap.com.br");

        assertNotNull(resultado);
        assertEquals("Thor", resultado.getNome());
        verify(usuarioPetRepository).save(any(UsuarioPet.class));
    }

    @Test
    @DisplayName("Deve retornar historico consolidado do pet")
    void deveRetornarHistoricoConsolidado() {
        Raca raca = Raca.builder().id(1L).nome("Golden").build();
        Pet pet = Pet.builder().id(10L).nome("Thor").raca(raca).dataNasc(LocalDate.now().minusYears(1)).build();
        Usuario usuario = Usuario.builder().id(1L).build();
        Status statusConcluido = Status.builder().id(2L).nomeStatus(EnumStatus.CONCLUIDO).build();

        Tarefa tarefa = Tarefa.builder()
                .id(100L)
                .titulo("Passeio")
                .pontosTarefa(20)
                .descricao("Passeio matinal")
                .criacao(LocalDateTime.now().minusDays(1))
                .prazo(LocalDateTime.now().plusDays(1))
                .conclusao(LocalDateTime.now())
                .status(statusConcluido)
                .usuario(usuario)
                .pet(pet)
                .build();

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(tarefaRepository.findAllByPetIdInAndStatusNomeStatusOrderByConclusaoDesc(List.of(10L), EnumStatus.CONCLUIDO)).thenReturn(List.of(tarefa));

        PetHistoryResponse response = petService.getConsolidatedHistory(10L);

        assertNotNull(response);
        assertEquals(10L, response.petId());
        assertEquals("Thor", response.nomePet());
        assertEquals(1, response.tarefasConcluidas().size());
        assertEquals("Passeio", response.tarefasConcluidas().get(0).titulo());
    }

    @Test
    @DisplayName("Deve calcular pontuacao total consolidada do pet via Stored Procedure Oracle (tarefas + aulas)")
    void deveCalcularPontuacaoTotal() {
        Raca raca = Raca.builder().id(1L).nome("SRD").build();
        Pet pet = Pet.builder().id(10L).nome("Luna").raca(raca).dataNasc(LocalDate.now().minusYears(1)).build();

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(petRepository.calcularPontuacaoPetNoBanco(10L)).thenReturn(Map.of(
                "p_pontos_tarefas", 50,
                "p_pontos_aulas", 30,
                "p_pontos_totais", 80
        ));

        PetPontuacaoResponse response = petService.calcularPontuacaoTotalPet(10L);

        assertNotNull(response);
        assertEquals(50, response.pontosTarefas());
        assertEquals(30, response.pontosAulas());
        assertEquals(80, response.pontosTotais());
    }

    @Test
    @DisplayName("Deve calcular pontuacao agregada dos pets do usuario via Stored Procedure Oracle")
    void deveCalcularPontuacaoAgregadaPetsUsuario() {
        Usuario usuario = Usuario.builder().id(1L).email("tutor@fiap.com.br").nome("Tutor").build();
        Map<String, Object> out = Map.of(
                "p_total_tarefas", 50,
                "p_total_aulas", 30,
                "p_total_geral", 80
        );

        when(usuarioRepository.findByEmailIgnoreCase("tutor@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(petRepository.calcularPontuacaoUsuarioNoBanco(1L)).thenReturn(out);
        when(usuarioPetRepository.findAllByUsuarioEmailIgnoreCase("tutor@fiap.com.br")).thenReturn(List.of());

        PetPontuacaoAgregadaResponse response = petService.calcularPontuacaoAgregadaPetsUsuario("tutor@fiap.com.br");

        assertNotNull(response);
        assertEquals(50, response.pontosTarefas());
        assertEquals(30, response.pontosAulas());
        assertEquals(80, response.pontosTotais());
        assertEquals(0, response.detalhePets().size());
    }

    @Test
    @DisplayName("Deve deletar pet chamando deleteById")
    void deveDeletarPet() {
        Pet pet = Pet.builder().id(10L).build();
        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));

        petService.delete(10L);

        verify(petRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Deve atualizar pet preservando dados e atualizando campos")
    void deveAtualizarPet() {
        var request = new PetRequest("Thor Atualizado", LocalDate.now().minusYears(2), "Golden Retriever", "GRANDE", 'M', true);
        Raca raca = Raca.builder().id(1L).nome("Golden Retriever").build();
        Pet petExistente = Pet.builder().id(10L).nome("Thor").raca(raca).build();

        when(petRepository.findById(10L)).thenReturn(Optional.of(petExistente));
        when(racaRepository.findByNomeIgnoreCase("Golden Retriever")).thenReturn(Optional.of(raca));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pet resultado = petService.update(10L, request);

        assertNotNull(resultado);
        assertEquals("Thor Atualizado", resultado.getNome());
    }

    @Test
    @DisplayName("Deve listar pets associados a um usuario")
    void deveListarPetsPorUsuario() {
        Pageable pageable = PageRequest.of(0, 10);
        Usuario usuario = Usuario.builder().id(1L).build();
        Pet pet = Pet.builder().id(10L).nome("Thor").build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(petRepository.findByUsuarioId(1L, pageable)).thenReturn(new PageImpl<>(List.of(pet)));

        Page<Pet> resultado = petService.findByUsuario(1L, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Thor", resultado.getContent().get(0).getNome());
    }

    @Test
    @DisplayName("Deve listar pets associados a um email de usuario")
    void deveListarPetsPorEmail() {
        Pageable pageable = PageRequest.of(0, 10);
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").build();
        Pet pet = Pet.builder().id(10L).nome("Thor").build();

        when(usuarioRepository.findByEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(petRepository.findByUsuarioId(1L, pageable)).thenReturn(new PageImpl<>(List.of(pet)));

        Page<Pet> resultado = petService.findByEmail("enzo@fiap.com.br", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Thor", resultado.getContent().get(0).getNome());
        verify(petRepository).findByUsuarioId(1L, pageable);
    }
}
