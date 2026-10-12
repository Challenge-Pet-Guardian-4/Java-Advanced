package fiap.com.br.petguardian.trilha.aula.dto;

import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.modulo.Modulo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AulaRequest(
        @NotBlank
        @Size(max = 50)
        String nome,

        @NotBlank
        @Size(max = 100)
        String descricao,

        @NotNull(message = "Pontos da aula são obrigatórios.")
        @Min(value = 0, message = "Pontos da aula não podem ser negativos.")
        @Max(value = 10, message = "Pontos da aula devem ser no máximo 10.")
        Integer pontosAula,

        @NotBlank
        @Size(max = 20)
        String dificuldade,

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
                .concluida(concluida)
                .modulo(modulo)
                .build();
    }
}
