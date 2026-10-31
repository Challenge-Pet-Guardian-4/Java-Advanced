package fiap.com.br.petguardian.trilha;

import fiap.com.br.petguardian.trilha.aula.DificuldadeAula;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.trilha.dto.TrilhaCompletaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetService;
import fiap.com.br.petguardian.trilha.aula.AulaService;
import java.util.Map;
import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAulaRepository;
import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.dto.TrilhaRequest;
import fiap.com.br.petguardian.trilha.modulo.Modulo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrilhaServiceTest {

    @Mock
    private TrilhaRepository trilhaRepository;

    @Mock
    private PetService petService;

    @Mock
    private AulaService aulaService;

    @Mock
    private ConteudoAulaRepository conteudoRepository;

    @InjectMocks
    private TrilhaService trilhaService;

    @Test
    @DisplayName("Deve criar trilha associada a um pet")
    void deveCriarTrilha() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        var request = new TrilhaRequest("Obediência Básica", "Primeiros comandos", 10L);
        Trilha trilhaSalva = Trilha.builder().id(1L).nome("Obediência Básica").descricao("Primeiros comandos").pet(pet).build();

        when(petService.findById(10L)).thenReturn(pet);
        when(trilhaRepository.save(any(Trilha.class))).thenReturn(trilhaSalva);

        Trilha resultado = trilhaService.create(request);

        assertNotNull(resultado);
        assertEquals("Obediência Básica", resultado.getNome());
        verify(trilhaRepository).save(any(Trilha.class));
    }

    @Test
    @DisplayName("Deve listar trilhas por pet")
    void deveListarTrilhasPorPet() {
        Pet pet = Pet.builder().id(10L).build();
        Trilha trilha = Trilha.builder().id(1L).nome("Truques").pet(pet).build();

        when(petService.findById(10L)).thenReturn(pet);
        when(trilhaRepository.findAllByPetId(10L)).thenReturn(List.of(trilha));

        List<Trilha> resultado = trilhaService.findAllByPetId(10L);

        assertEquals(1, resultado.size());
        assertEquals("Truques", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Deve deletar trilha e remover conteudos MongoDB em lote")
    void deveDeletarTrilhaComModulosEAulasERemoverConteudosNoMongo() {
        Aula aula1 = Aula.builder().id(101L).build();
        Aula aula2 = Aula.builder().id(102L).build();
        Modulo modulo1 = Modulo.builder().id(21L).aulas(java.util.Set.of(aula1)).build();
        Modulo modulo2 = Modulo.builder().id(22L).aulas(java.util.Set.of(aula2)).build();
        Trilha trilha = Trilha.builder().id(1L).modulos(java.util.Set.of(modulo1, modulo2)).build();

        when(trilhaRepository.findById(1L)).thenReturn(Optional.of(trilha));

        trilhaService.delete(1L);

        verify(conteudoRepository).deleteByAulaIdIn(argThat(ids -> ids.size() == 2 && ids.containsAll(List.of(101L, 102L))));
        verify(trilhaRepository).delete(trilha);
    }

    @Test
    @DisplayName("Deve atualizar trilha preservando modulos existentes")
    void deveAtualizarTrilhaPreservandoModulos() {
        Pet pet = Pet.builder().id(10L).build();
        Trilha trilhaExistente = Trilha.builder().id(1L).nome("Trilha Antiga").descricao("Desc").pet(pet).build();
        var request = new TrilhaRequest("Trilha Nova", "Nova Desc", 10L);

        when(trilhaRepository.findById(1L)).thenReturn(Optional.of(trilhaExistente));
        when(petService.findById(10L)).thenReturn(pet);
        when(trilhaRepository.save(any(Trilha.class))).thenAnswer(inv -> inv.getArgument(0));

        Trilha resultado = trilhaService.update(1L, request);

        assertNotNull(resultado);
        assertEquals("Trilha Nova", resultado.getNome());
        assertEquals(trilhaExistente.getModulos(), resultado.getModulos());
    }

    @Test
    @DisplayName("Deve listar trilhas completas com modulos e aulas por email do usuario")
    void deveListarTrilhasCompletasPorUsuarioEmail() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Trilha trilha = Trilha.builder().id(1L).nome("Truques").descricao("Desc").pet(pet).build();

        when(trilhaRepository.findAllByUsuarioEmail("tutor@teste.com")).thenReturn(List.of(trilha));

        var resultado = trilhaService.findTrilhasCompletasByUsuarioEmail("tutor@teste.com");

        assertEquals(1, resultado.size());
        assertEquals("Truques", resultado.get(0).nome());
        assertEquals("Thor", resultado.get(0).nomePet());
    }

    @Test
    @DisplayName("Deve listar todas as trilhas paginadas")
    void deveListarTodasTrilhas() {
        Pageable pageable = PageRequest.of(0, 10);
        Trilha trilha = Trilha.builder().id(1L).nome("Obediência").build();
        when(trilhaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(trilha)));

        Page<Trilha> resultado = trilhaService.findAll(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve buscar trilha por ID com sucesso")
    void deveBuscarTrilhaPorId() {
        Trilha trilha = Trilha.builder().id(1L).nome("Obediência").build();
        when(trilhaRepository.findById(1L)).thenReturn(Optional.of(trilha));

        Trilha resultado = trilhaService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao nao encontrar trilha por ID")
    void deveLancarExceptionAoNaoEncontrarTrilhaPorId() {
        when(trilhaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> trilhaService.findById(99L));
    }

    @Test
    @DisplayName("Deve buscar trilhas completas por usuario email com conteudos MongoDB")
    void deveBuscarTrilhasCompletasComConteudos() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Aula aula = Aula.builder().id(101L).nome("Senta").dificuldade(DificuldadeAula.FACIL).concluida(false).build();
        Modulo modulo = Modulo.builder().id(11L).nome("Módulo 1").aulas(java.util.Set.of(aula)).build();
        Trilha trilha = Trilha.builder().id(1L).nome("Obediência").pet(pet).modulos(java.util.Set.of(modulo)).build();

        when(trilhaRepository.findAllByUsuarioEmail("enzo@fiap.com.br")).thenReturn(List.of(trilha));
        when(aulaService.buscarConteudosEmLote(any())).thenReturn(Map.of(101L, "# Markdown de teste"));

        List<TrilhaCompletaResponse> resultado = trilhaService.findTrilhasCompletasByUsuarioEmail("enzo@fiap.com.br");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Obediência", resultado.get(0).nome());
    }


    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao criar trilha com pet inexistente")
    void deveLancarExceptionAoCriarTrilhaComPetInexistente() {
        when(petService.findById(99L)).thenThrow(new ResourceNotFoundException("Pet não encontrado"));
        assertThrows(ResourceNotFoundException.class, () -> trilhaService.create(new fiap.com.br.petguardian.trilha.dto.TrilhaRequest("Trilha", "Desc", 99L)));
    }

    @Test
    @DisplayName("Deve converter Trilha para TrilhaCompletaResponse usando fromEntity sem resolver")
    void deveConverterTrilhaCompletaResponseSemResolver() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Trilha trilha = Trilha.builder().id(1L).nome("Obediência").pet(pet).modulos(java.util.Set.of()).build();
        var resp = fiap.com.br.petguardian.trilha.dto.TrilhaCompletaResponse.fromEntity(trilha);
        assertNotNull(resp);
        assertEquals("Obediência", resp.nome());
    }

    @Test
    @DisplayName("Deve tratar falha no conteudo NoSQL ao buscar trilhas completas")
    void deveTratarFalhaNoConteudoNoSqlAoBuscarTrilhasCompletas() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Aula aula = Aula.builder().id(101L).nome("Senta").dificuldade(DificuldadeAula.FACIL).concluida(false).build();
        Modulo modulo = Modulo.builder().id(11L).nome("Módulo 1").aulas(java.util.Set.of(aula)).build();
        Trilha trilha = Trilha.builder().id(1L).nome("Obediência").pet(pet).modulos(java.util.Set.of(modulo)).build();

        when(trilhaRepository.findAllByUsuarioEmail("enzo@fiap.com.br")).thenReturn(List.of(trilha));
        when(aulaService.buscarConteudosEmLote(any())).thenReturn(Map.of());

        var resultado = trilhaService.findTrilhasCompletasByUsuarioEmail("enzo@fiap.com.br");
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }
}
