package fiap.com.br.petguardian.usuariopet.dto;

import jakarta.validation.constraints.NotNull;

public record TransferirResponsabilidadeRequest(
        @NotNull
        Long novoResponsavelId
) {}

