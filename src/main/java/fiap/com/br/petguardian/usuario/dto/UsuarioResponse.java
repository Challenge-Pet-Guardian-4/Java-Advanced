package fiap.com.br.petguardian.usuario.dto;

import fiap.com.br.petguardian.endereco.dto.EnderecoResponse;
import fiap.com.br.petguardian.usuario.Usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String role,
        String ddd,
        String numeroTelefone,
        EnderecoResponse endereco
) {
    public static UsuarioResponse fromEntity(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole().name(),
                usuario.getTelefone().getDdd(),
                usuario.getTelefone().getNumero(),
                EnderecoResponse.fromEntity(usuario.getEndereco())
        );
    }
}
