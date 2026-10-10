package fiap.com.br.petguardian.pet.dto;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.historico.Historico;
import fiap.com.br.petguardian.pet.historico.dto.HistoricoResponse;
import fiap.com.br.petguardian.tarefa.Tarefa;
import fiap.com.br.petguardian.tarefa.dto.TarefaResponse;
import fiap.com.br.petguardian.usuariopet.UsuarioPet;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorResponse;

import java.util.List;

public record PetDetailResponse(
        PetResponse pet,
        PetPontuacaoResponse pontos,
        List<CoCuidadorResponse> cuidadores,
        List<TarefaResponse> tarefasConcluidas,
        List<HistoricoResponse> historicos
) {
    public static PetDetailResponse of(
            Pet pet,
            PetPontuacaoResponse pontos,
            List<UsuarioPet> cuidadores,
            List<Tarefa> tarefas,
            List<Historico> historicos
    ) {
        return new PetDetailResponse(
                PetResponse.fromEntity(pet),
                pontos,
                cuidadores.stream().map(CoCuidadorResponse::fromEntity).toList(),
                tarefas.stream().map(TarefaResponse::fromEntity).toList(),
                historicos.stream().map(HistoricoResponse::fromEntity).toList()
        );
    }
}

