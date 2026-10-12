package fiap.com.br.petguardian.usuario.dto;

import java.util.List;

public record RedeCuidadoResponse(
        String emailUsuario,
        String nomeUsuario,
        List<PetResumo> pets,
        List<CuidadorResumo> coCuidadores,
        int totalTarefasPendentes,
        int totalTarefasConcluidas,
        int pontosAcumulados,
        String nivelGamificacao
) {
    public record PetResumo(
            Long id,
            String nome,
            String raca,
            boolean responsavelPrincipal,
            int totalTarefas
    ) {}

    public record CuidadorResumo(
            String nome,
            String email,
            boolean responsavelPrincipal,
            List<Long> petIds,
            List<String> petNomes,
            List<String> petsPrincipalNomes,
            List<String> petsAjudaNomes
    ) {}
}
