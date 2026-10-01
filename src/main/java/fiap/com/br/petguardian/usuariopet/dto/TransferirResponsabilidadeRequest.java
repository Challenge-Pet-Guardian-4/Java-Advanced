package fiap.com.br.petguardian.usuariopet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TransferirResponsabilidadeRequest(
        @NotBlank
        @Email
        String novoResponsavelEmail
) {}

