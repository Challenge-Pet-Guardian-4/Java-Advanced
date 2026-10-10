package fiap.com.br.petguardian.trilha;

import fiap.com.br.petguardian.trilha.dto.TrilhaCompletaResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trilhas")
@RequiredArgsConstructor
@Tag(name = "Trilhas", description = "Gerenciamento de trilhas de aprendizado e adestramento do pet")
public class TrilhaController {

    private final TrilhaService trilhaService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar árvore completa de trilhas, módulos e aulas dos pets do usuário autenticado")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    public List<TrilhaCompletaResponse> findMyTrilhas(Authentication authentication) {
        return trilhaService.findTrilhasCompletasByUsuarioEmail(authentication.getName());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todas as trilhas com paginação e ordenação")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    public Page<TrilhaResponse> findAll(
            @PageableDefault(size = 10, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return trilhaService.findAll(pageable)
                .map(TrilhaResponse::fromEntity);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar trilha por ID")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    public TrilhaResponse findById(@PathVariable Long id) {
        return TrilhaResponse.fromEntity(trilhaService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar nova trilha para um pet")
    @PreAuthorize("hasRole('ADMIN')")
    public TrilhaResponse create(@Valid @RequestBody TrilhaRequest request) {
        return TrilhaResponse.fromEntity(trilhaService.create(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar trilha")
    @PreAuthorize("hasRole('ADMIN')")
    public TrilhaResponse update(@PathVariable Long id, @Valid @RequestBody TrilhaRequest request) {
        return TrilhaResponse.fromEntity(trilhaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar trilha")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        trilhaService.delete(id);
    }
}
