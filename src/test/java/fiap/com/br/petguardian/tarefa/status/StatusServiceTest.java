package fiap.com.br.petguardian.tarefa.status;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    @Mock
    private StatusRepository statusRepository;

    @InjectMocks
    private StatusService statusService;

    @Test
    @DisplayName("Deve encontrar status por EnumStatus")
    void deveEncontrarStatusPorEnum() {
        Status status = Status.builder().id(1L).nomeStatus(EnumStatus.PENDENTE).build();
        when(statusRepository.findByNomeStatus(EnumStatus.PENDENTE)).thenReturn(Optional.of(status));

        Status resultado = statusService.findStatus(EnumStatus.PENDENTE);

        assertNotNull(resultado);
        assertEquals(EnumStatus.PENDENTE, resultado.getNomeStatus());
    }

    @Test
    @DisplayName("Deve encontrar status por nome String")
    void deveEncontrarStatusPorNomeString() {
        Status status = Status.builder().id(1L).nomeStatus(EnumStatus.PENDENTE).build();
        when(statusRepository.findByNomeStatus(EnumStatus.PENDENTE)).thenReturn(Optional.of(status));

        Status resultado = statusService.findStatusByNome("PENDENTE");

        assertNotNull(resultado);
        assertEquals(EnumStatus.PENDENTE, resultado.getNomeStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção se o status não for encontrado")
    void deveLancarExcecaoStatusNaoEncontrado() {
        when(statusRepository.findByNomeStatus(EnumStatus.PENDENTE)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> statusService.findStatus(EnumStatus.PENDENTE));
    }
}
