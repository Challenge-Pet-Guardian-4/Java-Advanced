package fiap.com.br.petguardian.pet.dto;

import fiap.com.br.petguardian.pet.historico.dto.HistoricoResponse;
import fiap.com.br.petguardian.tarefa.dto.TarefaResponse;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorResponse;
import java.util.List;

public record PetDetailResponse(
        PetResponse pet,
        PetPontuacaoResponse pontos,
        List<CoCuidadorResponse> cuidadores,
        List<TarefaResponse> tarefasConcluidas,
        List<HistoricoResponse> historicos
) {}
