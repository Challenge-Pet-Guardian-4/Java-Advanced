package fiap.com.br.petguardian.pet.dto;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetPorte;

import java.time.LocalDate;

public record PetResponse(
        Long id,
        String nome,
        LocalDate dataNasc,
        String raca,
        PetPorte porte,
        Character sexo,
        boolean castrado
) {
    public static PetResponse fromEntity(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getNome(),
                pet.getDataNasc(),
                pet.getRaca().getNome(),
                pet.getPorte(),
                pet.getSexo(),
                pet.isCastrado()
        );
    }
}
