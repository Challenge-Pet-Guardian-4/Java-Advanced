package fiap.com.br.petguardian.pet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
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
                extrairNumero(out, "p_pontos_tarefas", "P_PONTOS_TAREFAS"),
                extrairNumero(out, "p_pontos_aulas", "P_PONTOS_AULAS"),
                extrairNumero(out, "p_pontos_totais", "P_PONTOS_TOTAIS")
        );
    }

    static int extrairNumero(Map<String, Object> map, String keyLower, String keyUpper) {
        if (map == null) return 0;
        Object val = map.getOrDefault(keyLower, map.get(keyUpper));
        return val instanceof Number n ? n.intValue() : 0;
    }
}
