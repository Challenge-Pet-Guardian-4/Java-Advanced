package fiap.com.br.petguardian.trilha.aula.conteudo.dto;

import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAula;

import java.time.Instant;
import java.util.List;

public record ConteudoAulaResponse(
        String id,
        Long aulaId,
        String tipoConteudo,
        String corpoMarkdown,
        List<String> linksRecursos,
        Instant atualizadoEm
) {
    public static ConteudoAulaResponse fromEntity(ConteudoAula entity) {
        return new ConteudoAulaResponse(
                entity.getId(),
                entity.getAulaId(),
                entity.getTipoConteudo(),
                entity.getCorpoMarkdown(),
                entity.getLinksRecursos(),
                entity.getAtualizadoEm()
        );
    }
}
