package fiap.com.br.petguardian.pet.historico.dto;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.historico.Historico;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record HistoricoRequest(
        @NotBlank
        String tipoHist,

        @NotNull
        LocalDateTime dataHist,

        @NotNull
        Long petId
) {
    public Historico toEntity(Pet pet) {
        return Historico.builder()
                .tipoHist(tipoHist)
                .dataHist(dataHist)
                .pet(pet)
                .build();
    }
}
