package fiap.com.br.petguardian.trilha.aula.conteudo;

import fiap.com.br.petguardian.trilha.aula.conteudo.dto.ConteudoAulaRequest;
import fiap.com.br.petguardian.trilha.aula.conteudo.dto.ConteudoAulaResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aulas")
@RequiredArgsConstructor
@Tag(name = "Conteúdos NoSQL", description = "Gerenciamento de conteúdos dinâmicos no MongoDB")
public class ConteudoAulaController {

    private final ConteudoAulaService conteudoService;

    @GetMapping("/conteudos")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos os conteúdos das aulas no MongoDB com paginação e ordenação")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    public Page<ConteudoAulaResponse> findAll(
            @PageableDefault(size = 10, page = 0, sort = "aulaId", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return conteudoService.findAll(pageable)
                .map(ConteudoAulaResponse::fromEntity);
    }

    @GetMapping("/{aulaId}/conteudo")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar conteúdo rico de uma aula por ID da aula no MongoDB")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    public ConteudoAulaResponse findByAulaId(@PathVariable Long aulaId) {
        return ConteudoAulaResponse.fromEntity(conteudoService.findByAulaId(aulaId));
    }

    @PostMapping("/{aulaId}/conteudo")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar novo conteúdo rico para uma aula no MongoDB")
    @PreAuthorize("hasRole('ADMIN')")
    public ConteudoAulaResponse create(
            @PathVariable Long aulaId,
            @Valid @RequestBody ConteudoAulaRequest request
    ) {
        return ConteudoAulaResponse.fromEntity(conteudoService.create(aulaId, request));
    }

    @PutMapping("/{aulaId}/conteudo")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar conteúdo rico existente de uma aula no MongoDB")
    @PreAuthorize("hasRole('ADMIN')")
    public ConteudoAulaResponse update(
            @PathVariable Long aulaId,
            @Valid @RequestBody ConteudoAulaRequest request
    ) {
        return ConteudoAulaResponse.fromEntity(conteudoService.update(aulaId, request));
    }

    @DeleteMapping("/{aulaId}/conteudo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir conteúdo rico de uma aula no MongoDB")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long aulaId) {
        conteudoService.deleteByAulaId(aulaId);
    }
}
