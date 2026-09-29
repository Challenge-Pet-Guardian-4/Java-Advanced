package fiap.com.br.petguardian.usuariopet.dto;

import fiap.com.br.petguardian.usuariopet.UsuarioPet;

public record CoCuidadorResponse(
        Long usuarioId,
        String nome,
        String email,
        Long petId,
        String nomePet,
        boolean responsavelPrincipal
) {
    public static CoCuidadorResponse fromEntity(UsuarioPet up) {
        return new CoCuidadorResponse(
                up.getUsuario().getId(),
                up.getUsuario().getNome(),
                up.getUsuario().getEmail(),
                up.getPet().getId(),
                up.getPet().getNome(),
                up.isResponsavelPrincipal()
        );
    }
}
