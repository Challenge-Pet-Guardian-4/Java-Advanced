package fiap.com.br.petguardian.trilha.modulo.dto;

import fiap.com.br.petguardian.trilha.modulo.Modulo;

public record ModuloResponse(
        Long id,
        String nome,
        String tempoConclusao,
        String descricao,
        Long trilhaId,
        String nomeTrilha
) {
    public static ModuloResponse fromEntity(Modulo m) {
        return new ModuloResponse(
                m.getId(),
                m.getNome(),
                m.getTempoConclusao(),
                m.getDescricao(),
                m.getTrilha().getId(),
                m.getTrilha().getNome()
        );
    }
}
