package fiap.com.br.petguardian.usuario.dto;

import fiap.com.br.petguardian.endereco.dto.EnderecoResponse;
import fiap.com.br.petguardian.usuario.Usuario;

import java.util.Set;
import java.util.stream.Collectors;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String role,
        String ddd,
        String numeroTelefone,
        Set<EnderecoResponse> enderecos
) {
    public static UsuarioResponse fromEntity(Usuario usuario) {
        Set<EnderecoResponse> enderecoResponses = usuario.getEnderecos()
                .stream()
                .map(EnderecoResponse::fromEntity)
                .collect(Collectors.toSet());

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole().name(),
                usuario.getTelefone().getDdd(),
                usuario.getTelefone().getNumero(),
                enderecoResponses
        );
    }
}
