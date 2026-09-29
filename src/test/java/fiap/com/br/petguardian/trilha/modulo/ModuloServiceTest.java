package fiap.com.br.petguardian.trilha.modulo;

import fiap.com.br.petguardian.trilha.Trilha;
import fiap.com.br.petguardian.trilha.TrilhaRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModuloServiceTest {

    @Mock
    private ModuloRepository moduloRepository;

    @Mock
    private TrilhaRepository trilhaRepository;

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
    @DisplayName("Deve deletar modulo")
    void deveDeletarModulo() {
        Modulo modulo = Modulo.builder().id(10L).build();
        when(moduloRepository.findById(10L)).thenReturn(Optional.of(modulo));

        moduloService.delete(10L);

        verify(moduloRepository).deleteById(10L);
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
}
