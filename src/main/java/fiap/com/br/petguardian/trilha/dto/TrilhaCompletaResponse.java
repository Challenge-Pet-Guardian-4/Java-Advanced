package fiap.com.br.petguardian.trilha.dto;

import fiap.com.br.petguardian.trilha.Trilha;
import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.aula.DificuldadeAula;
import fiap.com.br.petguardian.trilha.modulo.Modulo;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public record TrilhaCompletaResponse(
        Long id,
        String nome,
        String descricao,
        Long petId,
        String nomePet,
        List<ModuloCompletoResponse> modulos
) {
    public static TrilhaCompletaResponse fromEntity(Trilha t, Function<Long, String> conteudoResolver) {
        List<ModuloCompletoResponse> modulos = t.getModulos().stream()
                .sorted(Comparator.comparing(Modulo::getId))
                .map(m -> ModuloCompletoResponse.fromEntity(m, conteudoResolver))
                .toList();

        return new TrilhaCompletaResponse(
                t.getId(),
                t.getNome(),
                t.getDescricao(),
                t.getPet().getId(),
                t.getPet().getNome(),
                modulos
        );
    }

    public static TrilhaCompletaResponse fromEntity(Trilha t) {
        return fromEntity(t, id -> null);
    }

    public record ModuloCompletoResponse(
            Long id,
            String nome,
            String tempoConclusao,
            String descricao,
            List<AulaCompletaResponse> aulas
    ) {
        public static ModuloCompletoResponse fromEntity(Modulo m, Function<Long, String> conteudoResolver) {
            List<AulaCompletaResponse> aulas = m.getAulas().stream()
                    .sorted(Comparator.comparing(Aula::getId))
                    .map(a -> AulaCompletaResponse.fromEntity(a, conteudoResolver.apply(a.getId())))
                    .toList();

            return new ModuloCompletoResponse(
                    m.getId(),
                    m.getNome(),
                    m.getTempoConclusao(),
                    m.getDescricao(),
                    aulas
            );
        }
    }

    public record AulaCompletaResponse(
            Long id,
            String nome,
            String descricao,
            Integer pontosAula,
            DificuldadeAula dificuldade,
            String conteudo,
            boolean concluida
    ) {
        public static AulaCompletaResponse fromEntity(Aula a, String conteudo) {
            return new AulaCompletaResponse(
                    a.getId(),
                    a.getNome(),
                    a.getDescricao(),
                    a.getPontosAula(),
                    a.getDificuldade(),
                    conteudo,
                    a.isConcluida()
            );
        }
    }
}
