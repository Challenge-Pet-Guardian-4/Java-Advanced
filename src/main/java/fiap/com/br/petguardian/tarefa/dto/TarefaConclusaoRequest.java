package fiap.com.br.petguardian.tarefa.dto;

import jakarta.validation.constraints.NotNull;

public record TarefaConclusaoRequest(
        @NotNull
        Long concluinteId
) {}

