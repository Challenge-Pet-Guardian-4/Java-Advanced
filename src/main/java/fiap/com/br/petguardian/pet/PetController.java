package fiap.com.br.petguardian.pet;

import fiap.com.br.petguardian.pet.dto.PetDetailResponse;
import fiap.com.br.petguardian.pet.dto.PetPontuacaoAgregadaResponse;
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

    // =========================================================================
    // 1. FLUXO DO USUÁRIO LOGADO (/me)
    // =========================================================================

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar pets associados ao usuário autenticado (como tutor principal ou co-cuidador)")
    public Page<PetResponse> findMyPets(
            Authentication authentication,
            @PageableDefault(size = 4, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return petService.findByEmail(authentication.getName(), pageable)
                .map(PetResponse::fromEntity);
    }

    @GetMapping("/me/pontos")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Consultar pontuação total acumulada de todos os pets do usuário autenticado via Stored Procedure Oracle (tarefas + aulas)")
    public PetPontuacaoAgregadaResponse getMyPetsPontos(Authentication authentication) {
        return petService.calcularPontuacaoAgregadaPetsUsuario(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar pet associado ao usuário autenticado como responsável principal")
    public PetResponse create(@Valid @RequestBody PetRequest petRequest, Authentication authentication) {
        return PetResponse.fromEntity(petService.create(petRequest, authentication.getName()));
    }

    // =========================================================================
    // 2. OPERAÇÕES DE GESTÃO DO PET POR ID (CUIDADOR / ADMIN)
    // =========================================================================

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar pet por ID")
    @PreAuthorize("hasRole('ADMIN') or @petService.isCuidadorDoPet(#id, authentication.name)")
    public PetResponse findById(@PathVariable Long id) {
        return PetResponse.fromEntity(petService.findById(id));
    }

    @GetMapping("/{id}/pontos")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Consultar pontuação total acumulada pelo pet via Stored Procedure Oracle (tarefas + aulas concluídas)")
    @PreAuthorize("hasRole('ADMIN') or @petService.isCuidadorDoPet(#id, authentication.name)")
    public PetPontuacaoResponse getPontosTotais(@PathVariable Long id) {
        return petService.calcularPontuacaoTotalPet(id);
    }

    @GetMapping("/{id}/detalhe")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Obter ficha consolidada do pet com pontuação, cuidadores, tarefas concluídas e prontuário")
    @PreAuthorize("hasRole('ADMIN') or @petService.isCuidadorDoPet(#id, authentication.name)")
    public PetDetailResponse getPetDetail(@PathVariable Long id) {
        return petService.getPetDetail(id);
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

    // =========================================================================
    // 3. OPERAÇÕES EXCLUSIVAS DE ADMINISTRAÇÃO (ROLE ADMIN)
    // =========================================================================

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos os pets com paginação e ordenação (somente ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PetResponse> findAll(
            @PageableDefault(size = 10, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return petService.findAll(pageable)
                .map(PetResponse::fromEntity);
    }

    @GetMapping("/by-usuario")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar pets associados a um usuário logado (somente ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PetResponse> findByUsuario(
            @RequestParam Long usuarioId,
            @PageableDefault(size = 20, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return petService.findByUsuario(usuarioId, pageable)
                .map(PetResponse::fromEntity);
    }
}
