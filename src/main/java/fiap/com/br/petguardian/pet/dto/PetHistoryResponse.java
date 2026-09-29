package fiap.com.br.petguardian.pet.dto;

import fiap.com.br.petguardian.tarefa.dto.TarefaResponse;

import java.util.List;

public record PetHistoryResponse(
        Long petId,
        String nomePet,
        List<TarefaResponse> tarefasConcluidas
) {
}
