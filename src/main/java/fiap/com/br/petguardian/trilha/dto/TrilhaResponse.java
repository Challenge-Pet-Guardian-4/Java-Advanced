package fiap.com.br.petguardian.trilha.dto;

import fiap.com.br.petguardian.trilha.Trilha;

public record TrilhaResponse(
        Long id,
        String nome,
        String descricao,
        Long petId,
        String nomePet
) {
    public static TrilhaResponse fromEntity(Trilha t) {
        return new TrilhaResponse(
                t.getId(),
                t.getNome(),
                t.getDescricao(),
                t.getPet().getId(),
                t.getPet().getNome()
        );
    }
}
