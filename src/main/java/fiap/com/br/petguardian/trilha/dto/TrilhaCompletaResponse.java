package fiap.com.br.petguardian.trilha.dto;

import fiap.com.br.petguardian.trilha.Trilha;
import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.modulo.Modulo;

import java.util.Comparator;
import java.util.List;

public record TrilhaCompletaResponse(
        Long id,
        String nome,
        String descricao,
        Long petId,
        String nomePet,
        List<ModuloCompletoResponse> modulos
) {
    public static TrilhaCompletaResponse fromEntity(Trilha t) {
        List<ModuloCompletoResponse> modulos = t.getModulos() == null ? List.of() :
                t.getModulos().stream()
                        .sorted(Comparator.comparing(Modulo::getId))
                        .map(ModuloCompletoResponse::fromEntity)
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

    public record ModuloCompletoResponse(
            Long id,
            String nome,
            String tempoConclusao,
            String descricao,
            List<AulaCompletaResponse> aulas
    ) {
        public static ModuloCompletoResponse fromEntity(Modulo m) {
            List<AulaCompletaResponse> aulas = m.getAulas() == null ? List.of() :
                    m.getAulas().stream()
                            .sorted(Comparator.comparing(Aula::getId))
                            .map(AulaCompletaResponse::fromEntity)
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
            String dificuldade,
            String conteudo,
            boolean concluida
    ) {
        public static AulaCompletaResponse fromEntity(Aula a) {
            return new AulaCompletaResponse(
                    a.getId(),
                    a.getNome(),
                    a.getDescricao(),
                    a.getPontosAula(),
                    a.getDificuldade(),
                    a.getConteudo(),
                    a.isConcluida()
            );
        }
    }
}
