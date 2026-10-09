package fiap.com.br.petguardian.pet.historico;

import fiap.com.br.petguardian.pet.historico.dto.HistoricoRequest;
import fiap.com.br.petguardian.pet.historico.dto.HistoricoResponse;
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
@RequestMapping("/historicos")
@RequiredArgsConstructor
@Tag(name = "Historico", description = "Histórico de saúde e eventos do pet")
public class HistoricoController {

    private final HistoricoService historicoService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar histórico agregado de todos os pets do cuidador autenticado")
    public List<HistoricoResponse> findMyHistorico(Authentication authentication) {
        return historicoService.findAllByUsuarioEmail(authentication.getName())
                .stream()
                .map(HistoricoResponse::fromEntity)
                .toList();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos os registros de histórico com paginação e ordenação (somente ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<HistoricoResponse> findAll(
            @PageableDefault(size = 10, page = 0, sort = "dataHist", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return historicoService.findAll(pageable)
                .map(HistoricoResponse::fromEntity);
    }

    @GetMapping("/pet/{petId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar historico de eventos/saude de um pet ordenado por data mais recente")
    @PreAuthorize("hasRole('ADMIN') or @petService.isCuidadorDoPet(#petId, authentication.name)")
    public List<HistoricoResponse> findByPetId(@PathVariable Long petId) {
        return historicoService.findAllByPetId(petId)
                .stream()
                .map(HistoricoResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar registro de historico por ID")
    @PreAuthorize("hasRole('ADMIN') or @historicoService.isCuidadorDoHistorico(#id, authentication.name)")
    public HistoricoResponse findById(@PathVariable Long id) {
        return HistoricoResponse.fromEntity(historicoService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar novo registro de historico para um pet")
    @PreAuthorize("hasRole('ADMIN') or @petService.isCuidadorDoPet(#request.petId(), authentication.name)")
    public HistoricoResponse create(@Valid @RequestBody HistoricoRequest request) {
        return HistoricoResponse.fromEntity(historicoService.create(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar registro de historico por ID")
    @PreAuthorize("hasRole('ADMIN') or (@historicoService.isCuidadorDoHistorico(#id, authentication.name) and @petService.isCuidadorDoPet(#request.petId(), authentication.name))")
    public HistoricoResponse update(@PathVariable Long id, @Valid @RequestBody HistoricoRequest request) {
        return HistoricoResponse.fromEntity(historicoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar registro de historico por ID")
    @PreAuthorize("hasRole('ADMIN') or @historicoService.isCuidadorDoHistorico(#id, authentication.name)")
    public void delete(@PathVariable Long id) {
        historicoService.delete(id);
    }
}
