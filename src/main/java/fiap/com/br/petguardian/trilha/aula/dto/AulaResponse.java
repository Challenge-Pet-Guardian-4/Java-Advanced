package fiap.com.br.petguardian.trilha.aula.dto;

import fiap.com.br.petguardian.trilha.aula.Aula;

public record AulaResponse(
        Long id,
        String nome,
        String descricao,
        Integer pontosAula,
        String dificuldade,
        String conteudo,
        boolean concluida,
        Long moduloId,
        String nomeModulo
) {
    public static AulaResponse fromEntity(Aula a) {
        return new AulaResponse(
                a.getId(),
                a.getNome(),
                a.getDescricao(),
                a.getPontosAula(),
                a.getDificuldade(),
                a.getConteudo(),
                a.isConcluida(),
                a.getModulo().getId(),
                a.getModulo().getNome()
        );
    }
}
