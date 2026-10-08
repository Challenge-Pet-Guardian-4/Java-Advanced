package fiap.com.br.petguardian.trilha.aula.conteudo.dto;

import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAula;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

public record ConteudoAulaRequest(
        @NotBlank
        String tipoConteudo,

        @NotBlank
        String corpoMarkdown,

        List<String> linksRecursos
) {
    public ConteudoAula toEntity(Long aulaId) {
        return ConteudoAula.builder()
                .aulaId(aulaId)
                .tipoConteudo(tipoConteudo)
                .corpoMarkdown(corpoMarkdown)
                .linksRecursos(linksRecursos)
                .atualizadoEm(Instant.now())
                .build();
    }
}
