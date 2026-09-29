package fiap.com.br.petguardian.endereco.dto;

import fiap.com.br.petguardian.endereco.Endereco;
import fiap.com.br.petguardian.endereco.bairro.Bairro;
import fiap.com.br.petguardian.validation.CepValidation;
import jakarta.validation.constraints.NotBlank;

public record EnderecoRequest(
        @NotBlank
        @CepValidation
        String cep,

        @NotBlank
        String numero
) {
    public Endereco toEntity(String rua, Bairro bairro) {
        return Endereco.builder()
                .cep(cep.replaceAll("\\D", ""))
                .numero(numero)
                .rua(rua)
                .bairro(bairro)
                .build();
    }
}
