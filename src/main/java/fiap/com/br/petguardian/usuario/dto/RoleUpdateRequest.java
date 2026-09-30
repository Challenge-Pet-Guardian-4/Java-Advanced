package fiap.com.br.petguardian.usuario.dto;

import fiap.com.br.petguardian.usuario.UsuarioRole;
import fiap.com.br.petguardian.validation.EnumValidation;
import jakarta.validation.constraints.NotBlank;

public record RoleUpdateRequest(
        @NotBlank
        @EnumValidation(enumClass = UsuarioRole.class)
        String role
) {}
