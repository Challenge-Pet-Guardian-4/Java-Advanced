package fiap.com.br.petguardian.usuariopet.dto;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuariopet.UsuarioPet;
import fiap.com.br.petguardian.usuariopet.UsuarioPetId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CoCuidadorRequest(
        @NotNull
        Long responsavelPrincipalId,

        @NotBlank
        @Email
        String email
) {
    public UsuarioPet toEntity(Usuario convidado, Pet pet) {
        return UsuarioPet.builder()
                .id(new UsuarioPetId(convidado.getId(), pet.getId()))
                .usuario(convidado)
                .pet(pet)
                .responsavelPrincipal(false)
                .build();
    }
}
