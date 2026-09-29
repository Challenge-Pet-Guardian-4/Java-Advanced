package fiap.com.br.petguardian.tarefa.dto;

import fiap.com.br.petguardian.tarefa.Tarefa;

import java.time.LocalDateTime;

public record TarefaResponse(
        Long id,
        String titulo,
        Integer pontosTarefa,
        String descricao,
        LocalDateTime criacao,
        LocalDateTime prazo,
        LocalDateTime conclusao,
        String status,
        Long usuarioId,
        Long petId
) {
    public static TarefaResponse fromEntity(Tarefa tarefa) {
        return new TarefaResponse(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getPontosTarefa(),
                tarefa.getDescricao(),
                tarefa.getCriacao(),
                tarefa.getPrazo(),
                tarefa.getConclusao(),
                tarefa.getStatus().getNomeStatus().name(),
                tarefa.getUsuario().getId(),
                tarefa.getPet().getId()
        );
    }
}
