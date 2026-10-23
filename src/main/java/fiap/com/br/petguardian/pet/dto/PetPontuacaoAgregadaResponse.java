package fiap.com.br.petguardian.pet.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import fiap.com.br.petguardian.util.DbUtils;

import java.util.Map;

public record PetPontuacaoAgregadaResponse(
        int pontosTarefas,
        int pontosAulas,
        int pontosTotais,
        @JsonRawValue
        String detalhePets
) {
    public static PetPontuacaoAgregadaResponse fromMap(Map<String, Object> out, String detalhePetsJson) {
        String json = (detalhePetsJson != null && !detalhePetsJson.isBlank()) ? detalhePetsJson : "[]";
        return new PetPontuacaoAgregadaResponse(
                DbUtils.getInt(out, "p_pontos_tarefas"),
                DbUtils.getInt(out, "p_pontos_aulas"),
                DbUtils.getInt(out, "p_pontos_totais"),
                json
        );
    }
}
