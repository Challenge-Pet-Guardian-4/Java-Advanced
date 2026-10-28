package fiap.com.br.petguardian.pet;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fiap.com.br.petguardian.pet.dto.PetDetailResponse;
import fiap.com.br.petguardian.pet.dto.PetPontuacaoAgregadaResponse;
import fiap.com.br.petguardian.pet.dto.PetPontuacaoResponse;
import fiap.com.br.petguardian.pet.dto.PetRequest;
import fiap.com.br.petguardian.pet.historico.HistoricoRepository;
import fiap.com.br.petguardian.pet.raca.Raca;
import fiap.com.br.petguardian.pet.raca.RacaRepository;
import fiap.com.br.petguardian.tarefa.TarefaRepository;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.trilha.Trilha;
import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAulaRepository;
import fiap.com.br.petguardian.trilha.modulo.Modulo;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    @Mock
    private HistoricoRepository historicoRepository;

    @Mock
    private ConteudoAulaRepository conteudoRepository;

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
        verify(petRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar detalhe consolidado do pet com historico e tarefas")
    void deveBuscarDetalheConsolidadoPet() {
        Raca raca = Raca.builder().id(1L).nome("Golden Retriever").build();
        Pet pet = Pet.builder().id(10L).nome("Thor").raca(raca).dataNasc(LocalDate.now().minusYears(2)).porte(PetPorte.GRANDE).build();

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(petRepository.calcularPontuacaoPetNoBanco(10L)).thenReturn(Map.of(
                "p_pontos_tarefas", 100,
                "p_pontos_aulas", 50,
                "p_pontos_totais", 150
        ));
        when(usuarioPetRepository.findAllByPetId(10L)).thenReturn(List.of());
        when(tarefaRepository.findAllByPetIdInAndStatusNomeStatusOrderByConclusaoDesc(List.of(10L), EnumStatus.CONCLUIDO))
                .thenReturn(List.of());
        when(historicoRepository.findAllByPetIdOrderByDataHistDesc(10L)).thenReturn(List.of());

        PetDetailResponse detail = petService.getPetDetail(10L);

        assertNotNull(detail);
        assertEquals("Thor", detail.pet().nome());
        assertEquals("Golden Retriever", detail.pet().raca());
        assertEquals(150, detail.pontos().pontosTotais());
        assertEquals(100, detail.pontos().pontosTarefas());
        assertEquals(50, detail.pontos().pontosAulas());
    }

    @Test
    @DisplayName("Deve calcular pontuacao total do pet chamando a procedure Oracle")
    void deveCalcularPontuacaoTotalPet() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();

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
    @DisplayName("Deve calcular pontuacao agregada dos pets do usuario via Stored Procedure Oracle com lista vazia")
    void deveCalcularPontuacaoAgregadaPetsUsuario() {
        Usuario usuario = Usuario.builder().id(1L).email("tutor@fiap.com.br").nome("Tutor").build();
        Map<String, Object> out = Map.of(
                "p_pontos_tarefas", 50,
                "p_pontos_aulas", 30,
                "p_pontos_totais", 80,
                "p_detalhe_pets_json", "[]"
        );

        when(usuarioRepository.findByEmail("tutor@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(petRepository.calcularPontuacaoUsuarioNoBanco(1L)).thenReturn(out);

        PetPontuacaoAgregadaResponse response = petService.calcularPontuacaoAgregadaPetsUsuario("tutor@fiap.com.br");

        assertNotNull(response);
        assertEquals(50, response.pontosTarefas());
        assertEquals(30, response.pontosAulas());
        assertEquals(80, response.pontosTotais());
        assertEquals("[]", response.detalhePets());
    }

    @Test
    @DisplayName("Deve calcular pontuacao agregada com detalhe dos pets via JSON da procedure sem N+1")
    void deveCalcularPontuacaoAgregadaPetsUsuarioComDetalhe() {
        Usuario usuario = Usuario.builder().id(1L).email("tutor@fiap.com.br").nome("Tutor").build();
        String jsonDetalhe = """
                [{"petId":10,"nomePet":"Thor","pontosTarefas":20,"pontosAulas":10,"pontosTotais":30},{"petId":11,"nomePet":"Luna","pontosTarefas":30,"pontosAulas":20,"pontosTotais":50}]""";
        Map<String, Object> out = Map.of(
                "p_pontos_tarefas", 50,
                "p_pontos_aulas", 30,
                "p_pontos_totais", 80,
                "p_detalhe_pets_json", jsonDetalhe
        );

        when(usuarioRepository.findByEmail("tutor@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(petRepository.calcularPontuacaoUsuarioNoBanco(1L)).thenReturn(out);

        PetPontuacaoAgregadaResponse response = petService.calcularPontuacaoAgregadaPetsUsuario("tutor@fiap.com.br");

        assertNotNull(response);
        assertEquals(50, response.pontosTarefas());
        assertEquals(30, response.pontosAulas());
        assertEquals(80, response.pontosTotais());
        assertEquals(jsonDetalhe, response.detalhePets());
    }

    @Test
    @DisplayName("Deve deletar pet sem trilhas chamando delete e limpando lista vazia no mongo")
    void deveDeletarPet() {
        Pet pet = Pet.builder().id(10L).build();
        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));

        petService.delete(10L);

        verify(petRepository).delete(pet);
        verify(conteudoRepository).deleteByAulaIdIn(List.of());
    }

    @Test
    @DisplayName("Deve deletar pet com trilhas e limpar conteudos de aula no mongo")
    void deveDeletarPetComTrilhasECascatasMongo() {
        Aula aula = Aula.builder().id(101L).build();
        Modulo modulo = Modulo.builder().id(201L).aulas(Set.of(aula)).build();
        Trilha trilha = Trilha.builder().id(301L).modulos(Set.of(modulo)).build();
        Pet pet = Pet.builder().id(10L).trilhas(Set.of(trilha)).build();

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));

        petService.delete(10L);

        verify(petRepository).delete(pet);
        verify(conteudoRepository).deleteByAulaIdIn(List.of(101L));
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
        when(petRepository.findByUsuarioPetsUsuarioId(1L, pageable)).thenReturn(new PageImpl<>(List.of(pet)));

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

        when(usuarioRepository.findByEmail("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(petRepository.findByUsuarioPetsUsuarioId(1L, pageable)).thenReturn(new PageImpl<>(List.of(pet)));

        Page<Pet> resultado = petService.findByEmail("enzo@fiap.com.br", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Thor", resultado.getContent().get(0).getNome());
        verify(petRepository).findByUsuarioPetsUsuarioId(1L, pageable);
    }

    @Test
    @DisplayName("Deve buscar pet por ID com sucesso")
    void deveBuscarPetPorId() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));

        Pet resultado = petService.findById(10L);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao nao encontrar pet por ID")
    void deveLancarExceptionAoNaoEncontrarPetPorId() {
        when(petRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> petService.findById(99L));
    }

    @Test
    @DisplayName("Deve criar pet associado ao usuario autenticado e gerar vinculo titular")
    void deveCriarPetComSucesso() {
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").build();
        var request = new PetRequest("Thor", LocalDate.now().minusYears(2), "Golden Retriever", "GRANDE", 'M', true);
        Raca raca = Raca.builder().id(100L).nome("Golden Retriever").build();
        Pet petSalvo = Pet.builder().id(10L).nome("Thor").raca(raca).porte(PetPorte.GRANDE).castrado(true).build();

        when(usuarioRepository.findByEmail("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(racaRepository.findByNomeIgnoreCase("Golden Retriever")).thenReturn(Optional.of(raca));
        when(petRepository.save(any(Pet.class))).thenReturn(petSalvo);

        Pet resultado = petService.create(request, "enzo@fiap.com.br");

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        verify(usuarioPetRepository).save(any());
    }

    @Test
    @DisplayName("Deve criar pet criando nova raca quando nao existir")
    void deveCriarPetComNovaRaca() {
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").build();
        var request = new PetRequest("Mel", LocalDate.now().minusYears(1), "Vira-lata", "MEDIO", 'F', false);
        Raca racaNova = Raca.builder().id(101L).nome("Vira-lata").build();
        Pet petSalvo = Pet.builder().id(11L).nome("Mel").raca(racaNova).porte(PetPorte.MEDIO).castrado(false).build();

        when(usuarioRepository.findByEmail("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(racaRepository.findByNomeIgnoreCase("Vira-lata")).thenReturn(Optional.empty());
        when(racaRepository.save(any(Raca.class))).thenReturn(racaNova);
        when(petRepository.save(any(Pet.class))).thenReturn(petSalvo);

        Pet resultado = petService.create(request, "enzo@fiap.com.br");

        assertNotNull(resultado);
        verify(racaRepository).save(any(Raca.class));
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao criar pet para usuario inexistente")
    void deveLancarExceptionAoCriarPetParaUsuarioInexistente() {
        var request = new PetRequest("Thor", LocalDate.now().minusYears(2), "Golden", "GRANDE", 'M', true);
        when(usuarioRepository.findByEmail("naoexiste@fiap.com.br")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> petService.create(request, "naoexiste@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve deletar pet com sucesso")
    void deveDeletarPetComSucesso() {
        Pet pet = Pet.builder().id(10L).nome("Thor").trilhas(Set.of()).build();
        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));

        petService.delete(10L);

        verify(petRepository).delete(pet);
        verify(conteudoRepository).deleteByAulaIdIn(List.of());
    }


    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao buscar pets por usuario inexistente")
    void deveLancarExceptionAoBuscarPetsPorUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> petService.findByUsuario(99L, org.springframework.data.domain.PageRequest.of(0, 10)));
    }

    @Test
    @DisplayName("Deve testar PetPontuacaoAgregadaResponse com JSON nulo ou vazio")
    void deveTestarPetPontuacaoAgregadaResponse() {
        var resp1 = fiap.com.br.petguardian.pet.dto.PetPontuacaoAgregadaResponse.fromMap(Map.of(), null);
        assertEquals("[]", resp1.detalhePets());
        var resp2 = fiap.com.br.petguardian.pet.dto.PetPontuacaoAgregadaResponse.fromMap(Map.of(), "   ");
        assertEquals("[]", resp2.detalhePets());
    }

}
