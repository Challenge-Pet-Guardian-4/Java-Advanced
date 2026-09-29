package fiap.com.br.petguardian.endereco.dto;

import fiap.com.br.petguardian.endereco.Endereco;

public record EnderecoResponse(
        Long id,
        String cep,
        String numero,
        String rua,
        String bairro,
        String cidade,
        String estado
) {
    public static EnderecoResponse fromEntity(Endereco endereco) {
        String bairro = endereco.getBairro().getNome();
        String cidade = endereco.getBairro().getCidade().getNome();
        String estado = endereco.getBairro().getCidade().getEstado().getNome();

        return new EnderecoResponse(
                endereco.getId(),
                endereco.getCep(),
                endereco.getNumero(),
                endereco.getRua(),
                bairro,
                cidade,
                estado
        );
    }
}
