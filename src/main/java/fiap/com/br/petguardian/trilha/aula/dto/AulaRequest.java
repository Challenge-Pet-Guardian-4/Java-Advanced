package fiap.com.br.petguardian.trilha.aula.dto;

import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.modulo.Modulo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AulaRequest(
        @NotBlank
        @Size(max = 50)
        String nome,

        @NotBlank
        @Size(max = 100)
        String descricao,

        @NotNull
        @Positive
        Integer pontosAula,

        @NotBlank
        @Size(max = 20)
        String dificuldade,

        @NotBlank
        @Size(max = 1000)
        String conteudo,

        @NotNull
        Boolean concluida,

        @NotNull
        Long moduloId
) {
    public Aula toEntity(Modulo modulo) {
        return Aula.builder()
                .nome(nome)
                .descricao(descricao)
                .pontosAula(pontosAula)
                .dificuldade(dificuldade)
                .conteudo(conteudo)
                .concluida(concluida)
                .modulo(modulo)
                .build();
    }
}
