package fiap.com.br.petguardian.trilha.modulo;

import fiap.com.br.petguardian.trilha.modulo.dto.ModuloRequest;
import fiap.com.br.petguardian.trilha.modulo.dto.ModuloResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/modulos")
@RequiredArgsConstructor
@Tag(name = "Modulos", description = "Gerenciamento de módulos das trilhas de aprendizado")
public class ModuloController {

    private final ModuloService moduloService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos os módulos com paginação e ordenação")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    public Page<ModuloResponse> findAll(
            @PageableDefault(size = 10, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return moduloService.findAll(pageable)
                .map(ModuloResponse::fromEntity);
    }

    @GetMapping("/trilha/{trilhaId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar modulos associados a uma trilha")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    public List<ModuloResponse> findByTrilhaId(@PathVariable Long trilhaId) {
        return moduloService.findAllByTrilhaId(trilhaId)
                .stream()
                .map(ModuloResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar modulo por ID")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    public ModuloResponse findById(@PathVariable Long id) {
        return ModuloResponse.fromEntity(moduloService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar novo modulo para uma trilha")
    @PreAuthorize("hasRole('ADMIN')")
    public ModuloResponse create(@Valid @RequestBody ModuloRequest request) {
        return ModuloResponse.fromEntity(moduloService.create(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar modulo")
    @PreAuthorize("hasRole('ADMIN')")
    public ModuloResponse update(@PathVariable Long id, @Valid @RequestBody ModuloRequest request) {
        return ModuloResponse.fromEntity(moduloService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar modulo")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        moduloService.delete(id);
    }
}
