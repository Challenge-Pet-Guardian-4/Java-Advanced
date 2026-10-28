package fiap.com.br.petguardian.trilha.modulo;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fiap.com.br.petguardian.trilha.Trilha;
import fiap.com.br.petguardian.trilha.TrilhaRepository;
import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAulaRepository;
import fiap.com.br.petguardian.trilha.modulo.dto.ModuloRequest;
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
class ModuloServiceTest {

    @Mock
    private ModuloRepository moduloRepository;

    @Mock
    private TrilhaRepository trilhaRepository;

    @Mock
    private ConteudoAulaRepository conteudoRepository;

    @InjectMocks
    private ModuloService moduloService;

    @Test
    @DisplayName("Deve criar modulo associado a uma trilha")
    void deveCriarModulo() {
        Trilha trilha = Trilha.builder().id(1L).nome("Obediência").build();
        var request = new ModuloRequest("Módulo 1", "5", "Comandos fundamentais", 1L);
        Modulo moduloSalvo = Modulo.builder().id(10L).nome("Módulo 1").tempoConclusao("5").descricao("Comandos fundamentais").trilha(trilha).build();

        when(trilhaRepository.findById(1L)).thenReturn(Optional.of(trilha));
        when(moduloRepository.save(any(Modulo.class))).thenReturn(moduloSalvo);

        Modulo resultado = moduloService.create(request);

        assertNotNull(resultado);
        assertEquals("Módulo 1", resultado.getNome());
        verify(moduloRepository).save(any(Modulo.class));
    }

    @Test
    @DisplayName("Deve listar modulos por trilha")
    void deveListarModulosPorTrilha() {
        Trilha trilha = Trilha.builder().id(1L).build();
        Modulo modulo = Modulo.builder().id(10L).nome("Módulo 1").trilha(trilha).build();

        when(trilhaRepository.findById(1L)).thenReturn(Optional.of(trilha));
        when(moduloRepository.findAllByTrilhaId(1L)).thenReturn(List.of(modulo));

        List<Modulo> resultado = moduloService.findAllByTrilhaId(1L);

        assertEquals(1, resultado.size());
        assertEquals("Módulo 1", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Deve deletar modulo e remover conteudos MongoDB em lote")
    void deveDeletarModuloComAulasERemoverConteudosNoMongo() {
        Aula aula1 = Aula.builder().id(101L).build();
        Aula aula2 = Aula.builder().id(102L).build();
        Modulo modulo = Modulo.builder().id(10L).aulas(java.util.Set.of(aula1, aula2)).build();

        when(moduloRepository.findById(10L)).thenReturn(Optional.of(modulo));

        moduloService.delete(10L);

        verify(conteudoRepository).deleteByAulaIdIn(argThat(ids -> ids.size() == 2 && ids.containsAll(List.of(101L, 102L))));
        verify(moduloRepository).delete(modulo);
    }

    @Test
    @DisplayName("Deve atualizar modulo preservando aulas existentes")
    void deveAtualizarModuloPreservandoAulas() {
        Trilha trilha = Trilha.builder().id(1L).build();
        Modulo moduloExistente = Modulo.builder().id(10L).nome("Modulo Antigo").tempoConclusao("5").descricao("Desc").trilha(trilha).build();
        var request = new ModuloRequest("Modulo Novo", "8", "Nova Desc", 1L);

        when(moduloRepository.findById(10L)).thenReturn(Optional.of(moduloExistente));
        when(trilhaRepository.findById(1L)).thenReturn(Optional.of(trilha));
        when(moduloRepository.save(any(Modulo.class))).thenAnswer(inv -> inv.getArgument(0));

        Modulo resultado = moduloService.update(10L, request);

        assertNotNull(resultado);
        assertEquals("Modulo Novo", resultado.getNome());
        assertEquals(moduloExistente.getAulas(), resultado.getAulas());
    }

    @Test
    @DisplayName("Deve listar todos os modulos paginados")
    void deveListarTodosModulos() {
        Pageable pageable = PageRequest.of(0, 10);
        Modulo modulo = Modulo.builder().id(10L).nome("Módulo 1").build();
        when(moduloRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(modulo)));

        Page<Modulo> resultado = moduloService.findAll(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve buscar modulo por ID com sucesso")
    void deveBuscarModuloPorId() {
        Modulo modulo = Modulo.builder().id(10L).nome("Módulo 1").build();
        when(moduloRepository.findById(10L)).thenReturn(Optional.of(modulo));

        Modulo resultado = moduloService.findById(10L);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao nao encontrar modulo por ID")
    void deveLancarExceptionAoNaoEncontrarModuloPorId() {
        when(moduloRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> moduloService.findById(99L));
    }


    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao criar modulo com trilha inexistente")
    void deveLancarExceptionAoCriarModuloComTrilhaInexistente() {
        when(trilhaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> moduloService.create(new fiap.com.br.petguardian.trilha.modulo.dto.ModuloRequest("Módulo 1", "2h", "Desc", 99L)));
    }

}
