package fiap.com.br.petguardian.pet.dto;

import java.util.List;

public record PetPontuacaoAgregadaResponse(
        int pontosTarefas,
        int pontosAulas,
        int pontosTotais,
        List<PetPontuacaoResponse> detalhePets
) {}
