package fiap.com.br.petguardian.pet.dto;

public record PetPontuacaoResponse(
        Long petId,
        String nomePet,
        int pontosTarefas,
        int pontosAulas,
        int pontosTotais
) {
}
