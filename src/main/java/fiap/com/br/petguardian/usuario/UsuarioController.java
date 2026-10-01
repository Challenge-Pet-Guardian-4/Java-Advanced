package fiap.com.br.petguardian.usuario;

import fiap.com.br.petguardian.usuario.dto.RedeCuidadoResponse;
import fiap.com.br.petguardian.usuario.dto.RoleUpdateRequest;
import fiap.com.br.petguardian.usuario.dto.UsuarioRequest;
import fiap.com.br.petguardian.usuario.dto.UsuarioResponse;
import fiap.com.br.petguardian.usuariopet.UsuarioPetService;
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
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuário", description = "Gerenciamento de usuários")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final UsuarioPetService usuarioPetService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar perfil do usuário autenticado")
    public UsuarioResponse getMe(Authentication authentication) {
        return UsuarioResponse.fromEntity(usuarioService.findByEmail(authentication.getName()));
    }

    @PutMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar perfil do usuário autenticado")
    public UsuarioResponse updateMe(Authentication authentication, @Valid @RequestBody UsuarioRequest usuarioRequest) {
        return UsuarioResponse.fromEntity(usuarioService.update(authentication.getName(), usuarioRequest));
    }

    @GetMapping("/me/rede-cuidado")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Visualizar rede de cuidado do usuário autenticado")
    @Tag(name = "Care Circle", description = "Gestão colaborativa de tutores e co-cuidadores do pet")
    public RedeCuidadoResponse getMyRedeCuidado(Authentication authentication) {
        return usuarioPetService.montarRedeCuidado(authentication.getName());
    }

    @PatchMapping("/me/upgrade-premium")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Realizar upgrade do perfil do usuário autenticado para PREMIUM")
    public UsuarioResponse upgradeMyPremium(Authentication authentication) {
        return UsuarioResponse.fromEntity(usuarioService.upgradePremium(authentication.getName()));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos os usuários com paginação e ordenação (somente ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UsuarioResponse> findAll(@PageableDefault(size = 10, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return usuarioService.findAll(pageable)
                .map(UsuarioResponse::fromEntity);
    }

    @GetMapping("/by-email")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar usuário por e-mail (somente ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse findByEmail(@RequestParam String email) {
        return UsuarioResponse.fromEntity(usuarioService.findByEmail(email));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar usuário por ID")
    @PreAuthorize("hasRole('ADMIN') or @usuarioService.isOwner(#id, authentication.name)")
    public UsuarioResponse findById(@PathVariable Long id) {
        return UsuarioResponse.fromEntity(usuarioService.findById(id));
    }

    @GetMapping("/{id}/rede-cuidado")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Visualizar rede de cuidado do usuário por ID (somente ADMIN)")
    @Tag(name = "Care Circle", description = "Gestão colaborativa de tutores e co-cuidadores do pet")
    @Tag(name = "Usuário", description = "Gerenciamento de usuários")
    @PreAuthorize("hasRole('ADMIN')")
    public RedeCuidadoResponse getRedeCuidado(@PathVariable Long id) {
        return usuarioPetService.montarRedeCuidado(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar usuário")
    public UsuarioResponse create(@Valid @RequestBody UsuarioRequest usuarioRequest) {
        return UsuarioResponse.fromEntity(usuarioService.create(usuarioRequest));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar usuário")
    @PreAuthorize("hasRole('ADMIN') or @usuarioService.isOwner(#id, authentication.name)")
    public UsuarioResponse update(@PathVariable Long id, @Valid @RequestBody UsuarioRequest usuarioRequest) {
        return UsuarioResponse.fromEntity(usuarioService.update(id, usuarioRequest));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar usuário")
    @PreAuthorize("hasRole('ADMIN') or @usuarioService.isOwner(#id, authentication.name)")
    public void delete(@PathVariable Long id) {
        usuarioService.delete(id);
    }

    @PatchMapping("/{id}/role")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Alterar role do usuário (somente ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return UsuarioResponse.fromEntity(usuarioService.updateRole(id, request.role()));
    }

    @PatchMapping("/{id}/upgrade-premium")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Realizar upgrade do perfil COMUM para PREMIUM (simulação de adesão ao plano)")
    @PreAuthorize("hasRole('ADMIN') or @usuarioService.isOwner(#id, authentication.name)")
    public UsuarioResponse upgradePremium(@PathVariable Long id) {
        return UsuarioResponse.fromEntity(usuarioService.upgradePremium(id));
    }
}
