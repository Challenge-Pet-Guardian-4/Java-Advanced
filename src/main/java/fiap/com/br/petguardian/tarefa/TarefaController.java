package fiap.com.br.petguardian.tarefa;

import fiap.com.br.petguardian.tarefa.dto.TarefaRequest;
import fiap.com.br.petguardian.tarefa.dto.TarefaResponse;
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

@RestController
@RequestMapping("/tarefas")
@RequiredArgsConstructor
@Tag(name = "Tarefas", description = "Gerenciamento de tarefas e rotinas de cuidados do pet")
public class TarefaController {
    private final TarefaService tarefaService;

    // =========================================================================
    // 1. FLUXO DO USUÁRIO / CUIDADOR LOGADO (/me)
    // =========================================================================

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar tarefas do cuidador autenticado com filtro opcional de status e paginação")
    public Page<TarefaResponse> findMyTarefas(
            Authentication authentication,
            @RequestParam(defaultValue = "ALL") String status,
            @PageableDefault(size = 10, page = 0, sort = "prazo", direction = Sort.Direction.ASC) Pageable pageable) {
        return tarefaService.findAllByEmail(authentication.getName(), status, pageable)
            .map(TarefaResponse::fromEntity);
    }

    @GetMapping("/me/pontos")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Consultar pontos totais acumulados pelo cuidador autenticado")
    public Integer calcularMyPontosTotais(Authentication authentication) {
        return tarefaService.calcularPontosTotaisEmail(authentication.getName());
    }

    // =========================================================================
    // 2. OPERAÇÕES DE GESTÃO DA TAREFA POR ID (CUIDADOR OU ADMIN)
    // =========================================================================

    @GetMapping("/by-pet/{petId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todas as tarefas de um pet específico com paginação")
    @PreAuthorize("hasRole('ADMIN') or @petService.isCuidadorDoPet(#petId, authentication.name)")
    public Page<TarefaResponse> findAllByPet(
            @PathVariable Long petId,
            @PageableDefault(size = 10, page = 0, sort = "prazo", direction = Sort.Direction.ASC) Pageable pageable) {
        return tarefaService.findAllByPetId(petId, pageable)
            .map(TarefaResponse::fromEntity);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar tarefa por ID")
    @PreAuthorize("hasRole('ADMIN') or @tarefaService.isCuidadorDaTarefa(#id, authentication.name)")
    public TarefaResponse findById(@PathVariable Long id) {
        return TarefaResponse.fromEntity(tarefaService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar tarefa")
    @PreAuthorize("hasRole('ADMIN') or @petService.isCuidadorDoPet(#tarefaRequest.petId(), authentication.name)")
    public TarefaResponse create(@Valid @RequestBody TarefaRequest tarefaRequest, Authentication authentication) {
        return TarefaResponse.fromEntity(tarefaService.create(tarefaRequest, authentication.getName()));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar tarefa")
    @PreAuthorize("hasRole('ADMIN') or @tarefaService.isCuidadorDaTarefa(#id, authentication.name)")
    public TarefaResponse update(@PathVariable Long id, @Valid @RequestBody TarefaRequest tarefaRequest, Authentication authentication) {
        return TarefaResponse.fromEntity(tarefaService.update(id, tarefaRequest, authentication.getName()));
    }

    @PatchMapping("/{id}/concluir")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Concluir tarefa")
    @PreAuthorize("hasRole('ADMIN') or @tarefaService.isCuidadorDaTarefa(#id, authentication.name)")
    public TarefaResponse concluir(@PathVariable Long id, Authentication authentication) {
        return TarefaResponse.fromEntity(tarefaService.concluir(id, authentication.getName()));
    }

    @PatchMapping("/{id}/desmarcar")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Desmarcar tarefa concluída retornando ao status PENDENTE")
    @PreAuthorize("hasRole('ADMIN') or @tarefaService.isCuidadorDaTarefa(#id, authentication.name)")
    public TarefaResponse desmarcar(@PathVariable Long id, Authentication authentication) {
        return TarefaResponse.fromEntity(tarefaService.desmarcar(id, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar tarefa")
    @PreAuthorize("hasRole('ADMIN') or @tarefaService.isCuidadorDaTarefa(#id, authentication.name)")
    public void delete(@PathVariable Long id) {
        tarefaService.delete(id);
    }

    // =========================================================================
    // 3. OPERAÇÕES EXCLUSIVAS DE ADMINISTRAÇÃO E INTEGRAÇÃO PROCEDURES (ROLE ADMIN)
    // =========================================================================

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todas as tarefas com paginação e ordenação")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<TarefaResponse> findAll(@PageableDefault(size = 10, page = 0, sort = "prazo", direction = Sort.Direction.ASC) Pageable pageable) {
        return tarefaService.findAll(pageable)
            .map(TarefaResponse::fromEntity);
    }

    @GetMapping("/by-usuario")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar tarefas do cuidador com filtro opcional de status e paginação (somente ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<TarefaResponse> findByUsuario(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "ALL") String status,
            @PageableDefault(size = 10, page = 0, sort = "prazo", direction = Sort.Direction.ASC) Pageable pageable) {
        return tarefaService.findAllByUsuario(usuarioId, status, pageable)
            .map(TarefaResponse::fromEntity);
    }

    @GetMapping("/by-usuario/pontos")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Consultar pontos totais acumulados pelo cuidador (somente ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public Integer calcularPontosTotaisUsuario(@RequestParam Long usuarioId) {
        return tarefaService.calcularPontosTotaisUsuario(usuarioId);
    }
}
