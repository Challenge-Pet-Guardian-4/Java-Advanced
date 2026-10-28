package fiap.com.br.petguardian.pet.historico;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetRepository;
import fiap.com.br.petguardian.pet.historico.dto.HistoricoRequest;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoServiceTest {

    @Mock
    private HistoricoRepository historicoRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private UsuarioPetRepository usuarioPetRepository;

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

    @Test
    @DisplayName("Deve verificar se usuário é cuidador do histórico")
    void deveVerificarCuidadorDoHistorico() {
        Pet pet = Pet.builder().id(10L).build();
        Historico hist = Historico.builder().id(1L).pet(pet).build();

        when(historicoRepository.findById(1L)).thenReturn(Optional.of(hist));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("enzo@fiap.com.br", 10L)).thenReturn(true);

        assertTrue(historicoService.isCuidadorDoHistorico(1L, "enzo@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve listar todos os historicos paginados")
    void deveListarHistoricosPaginados() {
        Pageable pageable = PageRequest.of(0, 10);
        Historico hist = Historico.builder().id(1L).tipoHist("Vacina").build();
        when(historicoRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(hist)));

        Page<Historico> resultado = historicoService.findAll(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve listar historico por email de usuario")
    void deveListarHistoricoPorEmail() {
        Historico hist = Historico.builder().id(1L).tipoHist("Vacina").build();
        when(historicoRepository.findAllByUsuarioEmailOrderByDataHistDesc("enzo@fiap.com.br")).thenReturn(List.of(hist));

        List<Historico> resultado = historicoService.findAllByUsuarioEmail("enzo@fiap.com.br");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Deve buscar historico por ID com sucesso")
    void deveBuscarHistoricoPorId() {
        Historico hist = Historico.builder().id(1L).tipoHist("Vacina").build();
        when(historicoRepository.findById(1L)).thenReturn(Optional.of(hist));

        Historico resultado = historicoService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao nao encontrar historico por ID")
    void deveLancarExceptionAoNaoEncontrarHistoricoPorId() {
        when(historicoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> historicoService.findById(99L));
    }

    @Test
    @DisplayName("Deve atualizar historico com sucesso")
    void deveAtualizarHistorico() {
        Pet pet = Pet.builder().id(10L).build();
        Historico hist = Historico.builder().id(1L).tipoHist("Vacina Antiga").build();
        var request = new HistoricoRequest("Vacina Nova", LocalDateTime.now(), 10L);

        when(historicoRepository.findById(1L)).thenReturn(Optional.of(hist));
        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(historicoRepository.save(any(Historico.class))).thenAnswer(inv -> inv.getArgument(0));

        Historico resultado = historicoService.update(1L, request);

        assertNotNull(resultado);
        assertEquals("Vacina Nova", resultado.getTipoHist());
    }


    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao buscar historicos por pet inexistente")
    void deveLancarExceptionAoBuscarHistoricosPorPetInexistente() {
        when(petRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> historicoService.findAllByPetId(99L));
    }

}
