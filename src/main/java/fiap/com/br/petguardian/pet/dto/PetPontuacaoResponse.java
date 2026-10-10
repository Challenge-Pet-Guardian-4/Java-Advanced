package fiap.com.br.petguardian.pet.dto;

import fiap.com.br.petguardian.util.DbUtils;

import java.util.Map;

public record PetPontuacaoResponse(
        Long petId,
        String nomePet,
        int pontosTarefas,
        int pontosAulas,
        int pontosTotais
) {
    public static PetPontuacaoResponse fromMap(Long petId, String nomePet, Map<String, Object> out) {
        return new PetPontuacaoResponse(
                petId,
                nomePet,
                DbUtils.getInt(out, "p_pontos_tarefas"),
                DbUtils.getInt(out, "p_pontos_aulas"),
                DbUtils.getInt(out, "p_pontos_totais")
        );
    }
}

