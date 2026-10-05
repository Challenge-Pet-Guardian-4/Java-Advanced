package fiap.com.br.petguardian.trilha.dto;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.trilha.Trilha;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrilhaRequest(
        @NotBlank
        @Size(max = 30)
        String nome,

        @NotBlank
        @Size(max = 200)
        String descricao,

        @NotNull
        Long petId
) {
    public Trilha toEntity(Pet pet) {
        return Trilha.builder()
                .nome(nome)
                .descricao(descricao)
                .pet(pet)
                .build();
    }
}
