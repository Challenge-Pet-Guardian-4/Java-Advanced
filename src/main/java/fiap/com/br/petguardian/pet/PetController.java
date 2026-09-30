package fiap.com.br.petguardian.pet;

import fiap.com.br.petguardian.pet.dto.PetHistoryResponse;
import fiap.com.br.petguardian.pet.dto.PetPontuacaoResponse;
import fiap.com.br.petguardian.pet.dto.PetRequest;
import fiap.com.br.petguardian.pet.dto.PetResponse;
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
@RequestMapping("/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Gerenciamento de pets e histórico clínico")
public class PetController {

    private final PetService petService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar pets com paginação e ordenação (somente ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PetResponse> findAll(
            @PageableDefault(size = 10, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return petService.findAll(pageable)
                .map(PetResponse::fromEntity);
    }

    @GetMapping("/by-usuario")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar pets associados a um usuário logado (como tutor principal ou co-cuidador)")
    @PreAuthorize("hasRole('ADMIN') or @usuarioService.isOwner(#usuarioId, authentication.name)")
    public Page<PetResponse> findByUsuario(
            @RequestParam Long usuarioId,
            @PageableDefault(size = 20, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return petService.findByUsuario(usuarioId, pageable)
                .map(PetResponse::fromEntity);
    }

    @GetMapping("/by-nome")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar pets por nome com paginação e ordenação (somente ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PetResponse> findByNome(@RequestParam String nome, @PageableDefault(size = 10, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return petService.findByNome(nome, pageable)
                .map(PetResponse::fromEntity);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar pet por ID")
    @PreAuthorize("hasRole('ADMIN') or @petService.isCuidadorDoPet(#id, authentication.name)")
    public PetResponse findById(@PathVariable Long id) {
        return PetResponse.fromEntity(petService.findById(id));
    }

    @GetMapping("/{id}/historico")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Obter histórico consolidado de cuidados do pet (tarefas concluídas)")
    @PreAuthorize("hasRole('ADMIN') or @petService.isCuidadorDoPet(#id, authentication.name)")
    public PetHistoryResponse getHistorico(@PathVariable Long id) {
        return petService.getConsolidatedHistory(id);
    }

    @GetMapping("/{id}/pontos")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Consultar pontuacao total acumulada pelo pet (tarefas + aulas concluidas)")
    @PreAuthorize("hasRole('ADMIN') or @petService.isCuidadorDoPet(#id, authentication.name)")
    public PetPontuacaoResponse getPontosTotais(@PathVariable Long id) {
        return petService.calcularPontuacaoTotalPet(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar pet")
    public PetResponse create(@Valid @RequestBody PetRequest petRequest, Authentication authentication) {
        return PetResponse.fromEntity(petService.create(petRequest, authentication.getName()));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar pet")
    @PreAuthorize("hasRole('ADMIN') or @petService.isCuidadorDoPet(#id, authentication.name)")
    public PetResponse update(@PathVariable Long id, @Valid @RequestBody PetRequest petRequest) {
        return PetResponse.fromEntity(petService.update(id, petRequest));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar pet (somente o responsável principal tem permissão)")
    @PreAuthorize("hasRole('ADMIN') or @petService.isResponsavelPrincipal(#id, authentication.name)")
    public void delete(@PathVariable Long id) {
        petService.delete(id);
    }
}
