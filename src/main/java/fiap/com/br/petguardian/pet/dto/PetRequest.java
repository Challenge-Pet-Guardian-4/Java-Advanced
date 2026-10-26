package fiap.com.br.petguardian.pet.dto;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetPorte;
import fiap.com.br.petguardian.pet.raca.Raca;
import fiap.com.br.petguardian.validation.DataNascValidation;
import fiap.com.br.petguardian.validation.EnumValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PetRequest(
        @NotBlank
        @Size(max = 30)
        String nome,

        @NotNull
        @PastOrPresent(message = "Data de nascimento não pode estar no futuro.")
        @DataNascValidation
        LocalDate dataNasc,

        @NotBlank
        @Size(max = 30)
        String raca,

        @NotNull
        @EnumValidation(enumClass = PetPorte.class)
        String porte,

        @NotNull
        Character sexo,

        @NotNull
        Boolean castrado
) {
    public Pet toEntity(Raca racaObj) {
        return Pet.builder()
                .nome(nome)
                .dataNasc(dataNasc)
                .raca(racaObj)
                .porte(PetPorte.valueOf(porte.trim().toUpperCase()))
                .sexo(sexo)
                .castrado(castrado)
                .build();
    }
}
