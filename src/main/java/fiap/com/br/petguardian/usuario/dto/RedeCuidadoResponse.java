package fiap.com.br.petguardian.usuario.dto;

import fiap.com.br.petguardian.util.DbUtils;

import java.util.List;
import java.util.Map;

public record RedeCuidadoResponse(
        String emailUsuario,
        String nomeUsuario,
        List<PetResumo> pets,
        List<CuidadorResumo> coCuidadores,
        int totalTarefasPendentes,
        int totalTarefasConcluidas,
        int pontosAcumulados
) {
    public static RedeCuidadoResponse from(
            String emailUsuario,
            String nomeUsuario,
            List<PetResumo> pets,
            List<CuidadorResumo> coCuidadores,
            Map<String, Object> resumoDb
    ) {
        return new RedeCuidadoResponse(
                emailUsuario,
                nomeUsuario,
                pets,
                coCuidadores,
                DbUtils.getInt(resumoDb, "p_tarefas_pendentes"),
                DbUtils.getInt(resumoDb, "p_tarefas_concluidas"),
                DbUtils.getInt(resumoDb, "p_pontos_acumulados")
        );
    }

    public record PetResumo(
            Long id,
            String nome,
            String raca,
            boolean responsavelPrincipal,
            List<Long> tarefaIds
    ) {}

    public record CuidadorResumo(
            String nome,
            String email,
            boolean responsavelPrincipal,
            List<Long> petIds,
            List<String> petNomes,
            List<String> petsPrincipalNomes,
            List<String> petsAjudaNomes
    ) {
        public CuidadorResumo(String nome, String email, boolean responsavelPrincipal, List<Long> petIds, List<String> petNomes) {
            this(nome, email, responsavelPrincipal, petIds, petNomes, List.of(), List.of());
        }
    }
}
