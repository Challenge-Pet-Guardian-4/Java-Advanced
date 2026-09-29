package fiap.com.br.petguardian.trilha;

import fiap.com.br.petguardian.trilha.dto.TrilhaRequest;
import fiap.com.br.petguardian.trilha.dto.TrilhaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trilhas")
@RequiredArgsConstructor
@Tag(name = "Trilhas", description = "Gerenciamento de trilhas de aprendizado e adestramento do pet")
public class TrilhaController {

    private final TrilhaService trilhaService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todas as trilhas com paginação e ordenação")
    public Page<TrilhaResponse> findAll(
            @PageableDefault(size = 10, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return trilhaService.findAll(pageable)
                .map(TrilhaResponse::fromEntity);
    }

    @GetMapping("/pet/{petId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar trilhas associadas a um pet")
    public List<TrilhaResponse> findByPetId(@PathVariable Long petId) {
        return trilhaService.findAllByPetId(petId)
                .stream()
                .map(TrilhaResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar trilha por ID")
    public TrilhaResponse findById(@PathVariable Long id) {
        return TrilhaResponse.fromEntity(trilhaService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar nova trilha para um pet")
    public TrilhaResponse create(@Valid @RequestBody TrilhaRequest request) {
        return TrilhaResponse.fromEntity(trilhaService.create(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar trilha")
    public TrilhaResponse update(@PathVariable Long id, @Valid @RequestBody TrilhaRequest request) {
        return TrilhaResponse.fromEntity(trilhaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar trilha")
    public void delete(@PathVariable Long id) {
        trilhaService.delete(id);
    }
}
