package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.tarefa.Tarefa;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.tarefa.status.Status;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaValidatorTest {

    @Mock
    private UsuarioPetRepository usuarioPetRepository;

    @InjectMocks
    private TarefaValidator validator;

    @Test
    @DisplayName("Deve permitir se o usuario for cuidador do pet")
    void devePermitirUsuarioVinculado() {
        when(usuarioPetRepository.existsByUsuarioIdAndPetId(1L, 10L)).thenReturn(true);

        assertDoesNotThrow(() -> validator.validarCuidadorDoPet(1L, 10L));
    }

    @Test
    @DisplayName("Deve lançar exceção se o usuário não for cuidador do pet")
    void deveLancarExcecaoUsuarioNaoVinculado() {
        when(usuarioPetRepository.existsByUsuarioIdAndPetId(1L, 10L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> validator.validarCuidadorDoPet(1L, 10L));
    }

    @Test
    @DisplayName("Deve permitir conclusão de tarefa com status PENDENTE")
    void devePermitirConclusaoStatusPendente() {
        Tarefa tarefa = Tarefa.builder()
                .status(Status.builder().nomeStatus(EnumStatus.PENDENTE).build())
                .build();

        assertDoesNotThrow(() -> validator.validarPendenteParaConclusao(tarefa));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar concluir tarefa já CONCLUIDA ou EXPIRADA")
    void deveLancarExcecaoTarefaJaConcluida() {
        Tarefa tarefaConcluida = Tarefa.builder()
                .status(Status.builder().nomeStatus(EnumStatus.CONCLUIDO).build())
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validarPendenteParaConclusao(tarefaConcluida));

        Tarefa tarefaExpirada = Tarefa.builder()
                .status(Status.builder().nomeStatus(EnumStatus.EXPIRADO).build())
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validarPendenteParaConclusao(tarefaExpirada));
    }

    @Test
    @DisplayName("Deve permitir desmarcar tarefa com status CONCLUIDO")
    void devePermitirDesmarcarStatusConcluido() {
        Tarefa tarefaConcluida = Tarefa.builder()
                .status(Status.builder().nomeStatus(EnumStatus.CONCLUIDO).build())
                .build();

        assertDoesNotThrow(() -> validator.validarConcluidaParaDesmarcar(tarefaConcluida));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar desmarcar tarefa que não esteja CONCLUIDA")
    void deveLancarExcecaoAoDesmarcarTarefaNaoConcluida() {
        Tarefa tarefaPendente = Tarefa.builder()
                .status(Status.builder().nomeStatus(EnumStatus.PENDENTE).build())
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validarConcluidaParaDesmarcar(tarefaPendente));

        Tarefa tarefaExpirada = Tarefa.builder()
                .status(Status.builder().nomeStatus(EnumStatus.EXPIRADO).build())
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validarConcluidaParaDesmarcar(tarefaExpirada));
    }
}
