package fiap.com.br.petguardian.trilha.aula.dto;

import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.aula.DificuldadeAula;

public record AulaResponse(
        Long id,
        String nome,
        String descricao,
        Integer pontosAula,
        DificuldadeAula dificuldade,
        String conteudo,
        boolean concluida,
        Long moduloId,
        String nomeModulo
) {
    public static AulaResponse fromEntity(Aula a, String conteudo) {
        return new AulaResponse(
                a.getId(),
                a.getNome(),
                a.getDescricao(),
                a.getPontosAula(),
                a.getDificuldade(),
                conteudo,
                a.isConcluida(),
                a.getModulo().getId(),
                a.getModulo().getNome()
        );
    }
}
