package fiap.com.br.petguardian.pet.dto;

import fiap.com.br.petguardian.util.DbUtils;

import java.util.List;
import java.util.Map;

public record PetPontuacaoAgregadaResponse(
        int pontosTarefas,
        int pontosAulas,
        int pontosTotais,
        List<PetPontuacaoResponse> detalhePets
) {
    public static PetPontuacaoAgregadaResponse fromMap(Map<String, Object> out, List<PetPontuacaoResponse> detalhePets) {
        return new PetPontuacaoAgregadaResponse(
                DbUtils.getInt(out, "p_pontos_tarefas"),
                DbUtils.getInt(out, "p_pontos_aulas"),
                DbUtils.getInt(out, "p_pontos_totais"),
                detalhePets
        );
    }
}

