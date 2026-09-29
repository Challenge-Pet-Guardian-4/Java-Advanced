package fiap.com.br.petguardian.endereco;

import fiap.com.br.petguardian.endereco.dto.ViaCepResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(
        url = "https://viacep.com.br/ws",
        accept = "application/json"
)
public interface ViaCepService {

    @GetExchange("/{cep}/json")
    ViaCepResponse getEnderecoPorCep(@PathVariable("cep") String cep);
}
