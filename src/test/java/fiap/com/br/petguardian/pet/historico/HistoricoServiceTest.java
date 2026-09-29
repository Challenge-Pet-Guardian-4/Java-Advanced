package fiap.com.br.petguardian.pet.historico;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetRepository;
import fiap.com.br.petguardian.pet.historico.dto.HistoricoRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoServiceTest {

    @Mock
    private HistoricoRepository historicoRepository;

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private HistoricoService historicoService;

    @Test
    @DisplayName("Deve registrar evento no histórico")
    void deveRegistrarHistorico() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        var request = new HistoricoRequest("Vacina V10", LocalDateTime.now(), 10L);
        Historico historico = Historico.builder().id(1L).tipoHist("Vacina V10").dataHist(LocalDateTime.now()).pet(pet).build();

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(historicoRepository.save(any(Historico.class))).thenReturn(historico);

        Historico resultado = historicoService.create(request);

        assertNotNull(resultado);
        assertEquals("Vacina V10", resultado.getTipoHist());
        verify(historicoRepository).save(any(Historico.class));
    }

    @Test
    @DisplayName("Deve listar histórico de um pet")
    void deveListarHistoricoDoPet() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Historico hist = Historico.builder().id(1L).tipoHist("Consulta Geral").dataHist(LocalDateTime.now()).pet(pet).build();

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(historicoRepository.findAllByPetIdOrderByDataHistDesc(10L)).thenReturn(List.of(hist));

        List<Historico> resultado = historicoService.findAllByPetId(10L);

        assertEquals(1, resultado.size());
        assertEquals("Consulta Geral", resultado.get(0).getTipoHist());
    }

    @Test
    @DisplayName("Deve deletar registro de histórico")
    void deveDeletarHistorico() {
        Historico hist = Historico.builder().id(1L).build();
        when(historicoRepository.findById(1L)).thenReturn(Optional.of(hist));

        historicoService.delete(1L);

        verify(historicoRepository).deleteById(1L);
    }
}
