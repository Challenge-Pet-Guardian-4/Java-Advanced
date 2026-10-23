package fiap.com.br.petguardian.usuariopet.dto;

import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuariopet.UsuarioPet;
import fiap.com.br.petguardian.util.DbUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public static RedeCuidadoResponse empty(Usuario usuario) {
        return new RedeCuidadoResponse(usuario.getEmail(), usuario.getNome(), List.of(), List.of(), 0, 0, 0, "BRONZE");
    }

    public static RedeCuidadoResponse of(
            Usuario usuario,
            List<UsuarioPet> vinculos,
            Map<Long, Integer> tarefasPorPet,
            List<UsuarioPet> todosVinculos,
            Map<String, Object> resumoDb,
            String nivel
    ) {
        var pets = vinculos.stream()
                .map(v -> PetResumo.of(v, tarefasPorPet.getOrDefault(v.getPet().getId(), 0)))
                .toList();

        var cuidadores = todosVinculos.stream()
                .filter(v -> !v.getUsuario().getEmail().equalsIgnoreCase(usuario.getEmail()))
                .collect(Collectors.groupingBy(v -> v.getUsuario().getId()))
                .values().stream()
                .map(CuidadorResumo::fromGroup)
                .toList();

        return new RedeCuidadoResponse(
                usuario.getEmail(),
                usuario.getNome(),
                pets,
                cuidadores,
                DbUtils.getInt(resumoDb, "p_tarefas_pendentes"),
                DbUtils.getInt(resumoDb, "p_tarefas_concluidas"),
                DbUtils.getInt(resumoDb, "p_pontos_acumulados"),
                nivel != null ? nivel : "BRONZE"
        );
    }

    public record PetResumo(
            Long id,
            String nome,
            String raca,
            boolean responsavelPrincipal,
            int totalTarefas
    ) {
        public static PetResumo of(UsuarioPet v, int tarefas) {
            return new PetResumo(v.getPet().getId(), v.getPet().getNome(), v.getPet().getRaca().getNome(), v.isResponsavelPrincipal(), tarefas);
        }
    }

    public record CuidadorResumo(
            String nome,
            String email,
            boolean responsavelPrincipal,
            List<Long> petIds,
            List<String> petNomes,
            List<String> petsPrincipalNomes,
            List<String> petsAjudaNomes
    ) {
        public static CuidadorResumo fromGroup(List<UsuarioPet> vinculos) {
            Usuario u = vinculos.get(0).getUsuario();
            var titular = vinculos.stream().filter(UsuarioPet::isResponsavelPrincipal).map(v -> v.getPet().getNome()).toList();
            var apoio = vinculos.stream().filter(v -> !v.isResponsavelPrincipal()).map(v -> v.getPet().getNome()).toList();
            return new CuidadorResumo(
                    u.getNome(),
                    u.getEmail(),
                    !titular.isEmpty(),
                    vinculos.stream().map(v -> v.getPet().getId()).toList(),
                    vinculos.stream().map(v -> v.getPet().getNome()).toList(),
                    titular,
                    apoio
            );
        }
    }
}
