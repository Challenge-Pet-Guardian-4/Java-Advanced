package fiap.com.br.petguardian.endereco;

import fiap.com.br.petguardian.endereco.dto.EnderecoRequest;
import fiap.com.br.petguardian.endereco.dto.EnderecoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/enderecos")
@RequiredArgsConstructor
@Tag(name = "Endereco", description = "Gerenciamento de endereços")
public class EnderecoController {
    private final EnderecoService enderecoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos os enderecos com paginação e ordenação")
    public Page<EnderecoResponse> findAll(@PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return enderecoService.findAll(pageable)
            .map(EnderecoResponse::fromEntity);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar endereco por ID")
    public EnderecoResponse findById(@PathVariable Long id) {
        return EnderecoResponse.fromEntity(enderecoService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar endereco")
    public EnderecoResponse create(@Valid @RequestBody EnderecoRequest enderecoRequest) {
        return EnderecoResponse.fromEntity(enderecoService.create(enderecoRequest));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar endereco")
    public EnderecoResponse update(@PathVariable Long id, @Valid @RequestBody EnderecoRequest enderecoRequest) {
        return EnderecoResponse.fromEntity(enderecoService.update(id, enderecoRequest));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar endereco")
    public void delete(@PathVariable Long id) {
        enderecoService.delete(id);
    }
}
