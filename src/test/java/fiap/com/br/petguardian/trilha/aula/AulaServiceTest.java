package fiap.com.br.petguardian.trilha.aula;

import fiap.com.br.petguardian.trilha.aula.dto.AulaRequest;
import fiap.com.br.petguardian.trilha.modulo.Modulo;
import fiap.com.br.petguardian.trilha.modulo.ModuloRepository;
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
class AulaServiceTest {

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private ModuloRepository moduloRepository;

    @InjectMocks
    private AulaService aulaService;

    @Test
    @DisplayName("Deve criar aula associada a um modulo")
    void deveCriarAula() {
        Modulo modulo = Modulo.builder().id(10L).nome("Módulo 1").build();
        var request = new AulaRequest("Comando Senta!", "Ensine o pet a sentar", 25, "FACIL", "Passo 1: petisco", false, 10L);
        Aula aulaSalva = Aula.builder().id(100L).nome("Comando Senta!").descricao("Ensine o pet a sentar").pontosAula(25).dificuldade("FACIL").conteudo("Passo 1: petisco").concluida(false).modulo(modulo).build();

        when(moduloRepository.findById(10L)).thenReturn(Optional.of(modulo));
        when(aulaRepository.save(any(Aula.class))).thenReturn(aulaSalva);

        Aula resultado = aulaService.create(request);

        assertNotNull(resultado);
        assertEquals("Comando Senta!", resultado.getNome());
        assertEquals(25, resultado.getPontosAula());
        verify(aulaRepository).save(any(Aula.class));
    }

    @Test
    @DisplayName("Deve listar aulas por modulo")
    void deveListarAulasPorModulo() {
        Modulo modulo = Modulo.builder().id(10L).build();
        Aula aula = Aula.builder().id(100L).nome("Comando Senta!").modulo(modulo).build();

        when(moduloRepository.findById(10L)).thenReturn(Optional.of(modulo));
        when(aulaRepository.findAllByModuloId(10L)).thenReturn(List.of(aula));

        List<Aula> resultado = aulaService.findAllByModuloId(10L);

        assertEquals(1, resultado.size());
        assertEquals("Comando Senta!", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Deve concluir aula marcando concluida como true")
    void deveConcluirAula() {
        Aula aula = Aula.builder().id(100L).concluida(false).build();
        when(aulaRepository.findById(100L)).thenReturn(Optional.of(aula));
        when(aulaRepository.save(any(Aula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Aula resultado = aulaService.concluir(100L);

        assertTrue(resultado.isConcluida());
        verify(aulaRepository).save(aula);
    }

    @Test
    @DisplayName("Deve deletar aula")
    void deveDeletarAula() {
        Aula aula = Aula.builder().id(100L).build();
        when(aulaRepository.findById(100L)).thenReturn(Optional.of(aula));

        aulaService.delete(100L);

        verify(aulaRepository).deleteById(100L);
    }
}
