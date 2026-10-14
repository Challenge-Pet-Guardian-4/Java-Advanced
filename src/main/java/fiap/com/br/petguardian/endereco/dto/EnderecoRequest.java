package fiap.com.br.petguardian.endereco.dto;

import fiap.com.br.petguardian.endereco.Endereco;
import fiap.com.br.petguardian.endereco.bairro.Bairro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(
        @NotBlank
        @Pattern(regexp = "^\\d{5}-?\\d{3}$")
        String cep,

        @NotBlank
        @Size(max = 5)
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
