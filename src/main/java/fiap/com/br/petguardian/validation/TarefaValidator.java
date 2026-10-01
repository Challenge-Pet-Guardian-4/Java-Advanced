package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.tarefa.Tarefa;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TarefaValidator {

    private final UsuarioPetRepository usuarioPetRepository;

    public void validarCuidadorDoPet(String email, Long petId) {
        if (!usuarioPetRepository.existsByUsuarioEmailAndPetId(email.trim(), petId)) {
            throw new IllegalArgumentException("Usuario informado nao esta vinculado ao pet da tarefa.");
        }
    }

    public void validarPendenteParaConclusao(Tarefa tarefa) {
        if (tarefa.getStatus().getNomeStatus() != EnumStatus.PENDENTE) {
            throw new IllegalArgumentException("Apenas tarefas pendentes podem ser concluidas.");
        }
    }

    public void validarConcluidaParaDesmarcar(Tarefa tarefa) {
        if (tarefa.getStatus().getNomeStatus() != EnumStatus.CONCLUIDO) {
            throw new IllegalArgumentException("Apenas tarefas concluidas podem ser desmarcadas.");
        }
    }
}
