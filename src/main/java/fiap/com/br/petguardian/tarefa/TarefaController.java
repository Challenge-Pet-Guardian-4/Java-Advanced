package fiap.com.br.petguardian.tarefa;

import fiap.com.br.petguardian.tarefa.dto.TarefaConclusaoRequest;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tarefas")
@RequiredArgsConstructor
@Tag(name = "Tarefas", description = "Gerenciamento de tarefas e rotinas de cuidados do pet")
public class TarefaController {
    private final TarefaService tarefaService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todas as tarefas com paginação e ordenação")
    public Page<TarefaResponse> findAll(@PageableDefault(size = 10, page = 0, sort = "prazo", direction = Sort.Direction.ASC) Pageable pageable) {
        return tarefaService.findAll(pageable)
            .map(TarefaResponse::fromEntity);
    }

    @GetMapping("/by-usuario")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar tarefas por usuarioId com filtro opcional de status e paginação")
    public Page<TarefaResponse> findAllByUsuario(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "ALL") String status,
            @PageableDefault(size = 10, page = 0, sort = "prazo", direction = Sort.Direction.ASC) Pageable pageable) {
        return tarefaService.findAllByUsuario(usuarioId, status, pageable)
            .map(TarefaResponse::fromEntity);
    }

    @GetMapping("/by-pet/{petId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todas as tarefas de um pet específico com paginação")
    public Page<TarefaResponse> findAllByPet(
            @PathVariable Long petId,
            @PageableDefault(size = 10, page = 0, sort = "prazo", direction = Sort.Direction.ASC) Pageable pageable) {
        return tarefaService.findAllByPetId(petId, pageable)
            .map(TarefaResponse::fromEntity);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar tarefa por ID")
    public TarefaResponse findById(@PathVariable Long id) {
        return TarefaResponse.fromEntity(tarefaService.findById(id));
    }

    @GetMapping("/by-usuario/{usuarioId}/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar tarefa por usuario e ID")
    public TarefaResponse findByUsuarioIdAndTarefaId(@PathVariable Long usuarioId, @PathVariable Long id) {
        return TarefaResponse.fromEntity(tarefaService.findByUsuarioIdAndTarefaId(usuarioId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar tarefa")
    public TarefaResponse create(@Valid @RequestBody TarefaRequest tarefaRequest) {
        return TarefaResponse.fromEntity(tarefaService.create(tarefaRequest));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar tarefa")
    public TarefaResponse update(@PathVariable Long id, @Valid @RequestBody TarefaRequest tarefaRequest) {
        return TarefaResponse.fromEntity(tarefaService.update(id, tarefaRequest));
    }

    @PatchMapping("/{id}/concluir")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Concluir tarefa")
    public TarefaResponse concluir(@PathVariable Long id, @Valid @RequestBody TarefaConclusaoRequest tarefaConclusaoRequest) {
        return TarefaResponse.fromEntity(tarefaService.concluir(id, tarefaConclusaoRequest));
    }

    @PatchMapping("/{id}/desmarcar")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Desmarcar tarefa concluída retornando ao status PENDENTE")
    public TarefaResponse desmarcar(@PathVariable Long id, @RequestParam Long usuarioId) {
        return TarefaResponse.fromEntity(tarefaService.desmarcar(id, usuarioId));
    }

    @GetMapping("/by-usuario/pontos")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Consultar pontos totais acumulados por um cuidador")
    public Integer calcularPontosTotaisUsuario(@RequestParam Long usuarioId) {
        return tarefaService.calcularPontosTotaisUsuario(usuarioId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar tarefa")
    public void delete(@PathVariable Long id) {
        tarefaService.delete(id);
    }
}
