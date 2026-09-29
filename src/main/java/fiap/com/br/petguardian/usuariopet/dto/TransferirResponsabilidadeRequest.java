package fiap.com.br.petguardian.usuariopet.dto;

import fiap.com.br.petguardian.validation.DiferentesUsuariosValidation;
import jakarta.validation.constraints.NotNull;

@DiferentesUsuariosValidation
public record TransferirResponsabilidadeRequest(
        @NotNull
        Long responsavelAtualId,

        @NotNull
        Long novoResponsavelId
) {}
