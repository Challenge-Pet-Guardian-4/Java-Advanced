package fiap.com.br.petguardian.pet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PetPontuacaoAgregadaResponse(
        int pontosTarefas,
        int pontosAulas,
        int pontosTotais,
        List<PetPontuacaoResponse> detalhePets
) {
    public static PetPontuacaoAgregadaResponse fromMap(Map<String, Object> out, List<PetPontuacaoResponse> detalhePets) {
        return new PetPontuacaoAgregadaResponse(
                PetPontuacaoResponse.extrairNumero(out, "p_total_tarefas", "P_TOTAL_TAREFAS"),
                PetPontuacaoResponse.extrairNumero(out, "p_total_aulas", "P_TOTAL_AULAS"),
                PetPontuacaoResponse.extrairNumero(out, "p_total_geral", "P_TOTAL_GERAL"),
                detalhePets
        );
    }
}
