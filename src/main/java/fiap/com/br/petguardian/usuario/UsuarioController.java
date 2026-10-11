package fiap.com.br.petguardian.usuario;

import fiap.com.br.petguardian.auth.TokenService;
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
import org.springframework.http.ResponseEntity;
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
    private final TokenService tokenService;

    // =========================================================================
    // 1. FLUXO DO USUÁRIO LOGADO (/me)
    // =========================================================================

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "[AUTENTICADO] Buscar perfil do usuário autenticado")
    public UsuarioResponse getMe(Authentication authentication) {
        return UsuarioResponse.fromEntity(usuarioService.findByEmail(authentication.getName()));
    }

    @PutMapping("/me")
    @Operation(summary = "[AUTENTICADO] Atualizar perfil do usuário autenticado")
    public ResponseEntity<UsuarioResponse> updateMe(Authentication authentication, @Valid @RequestBody UsuarioRequest usuarioRequest) {
        Usuario usuario = usuarioService.update(authentication.getName(), usuarioRequest);
        return ResponseEntity.ok()
                .header("X-Novo-Token", tokenService.generateToken(usuario.getEmail()))
                .body(UsuarioResponse.fromEntity(usuario));
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "[AUTENTICADO] Deletar perfil do usuário autenticado")
    public void deleteMe(Authentication authentication) {
        usuarioService.delete(authentication.getName());
    }

    @GetMapping("/me/rede-cuidado")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "[AUTENTICADO] Visualizar rede de cuidado do usuário autenticado")
    @Tag(name = "Care Circle", description = "Gestão colaborativa de tutores e co-cuidadores do pet")
    public RedeCuidadoResponse getMyRedeCuidado(Authentication authentication) {
        return usuarioPetService.montarRedeCuidado(authentication.getName());
    }

    @PatchMapping("/me/upgrade-premium")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "[AUTENTICADO] Realizar upgrade do perfil do usuário autenticado para PREMIUM")
    public UsuarioResponse upgradeMyPremium(Authentication authentication) {
        return UsuarioResponse.fromEntity(usuarioService.upgradePremium(authentication.getName()));
    }

    // =========================================================================
    // 2. CADASTRO PÚBLICO E OPERAÇÕES POR ID (DONO OU ADMIN)
    // =========================================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "[PÚBLICO] Criar usuário (cadastro público de tutor)")
    public UsuarioResponse create(@Valid @RequestBody UsuarioRequest usuarioRequest) {
        return UsuarioResponse.fromEntity(usuarioService.create(usuarioRequest));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "[DONO / ADMIN] Buscar usuário por ID")
    @PreAuthorize("hasRole('ADMIN') or @usuarioService.isOwner(#id, authentication.name)")
    public UsuarioResponse findById(@PathVariable Long id) {
        return UsuarioResponse.fromEntity(usuarioService.findById(id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "[DONO / ADMIN] Atualizar usuário por ID")
    @PreAuthorize("hasRole('ADMIN') or @usuarioService.isOwner(#id, authentication.name)")
    public UsuarioResponse update(@PathVariable Long id, @Valid @RequestBody UsuarioRequest usuarioRequest) {
        return UsuarioResponse.fromEntity(usuarioService.update(id, usuarioRequest));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "[DONO / ADMIN] Deletar usuário por ID")
    @PreAuthorize("hasRole('ADMIN') or @usuarioService.isOwner(#id, authentication.name)")
    public void delete(@PathVariable Long id) {
        usuarioService.delete(id);
    }

    @PatchMapping("/{id}/upgrade-premium")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "[DONO / ADMIN] Realizar upgrade do perfil COMUM para PREMIUM")
    @PreAuthorize("hasRole('ADMIN') or @usuarioService.isOwner(#id, authentication.name)")
    public UsuarioResponse upgradePremium(@PathVariable Long id) {
        return UsuarioResponse.fromEntity(usuarioService.upgradePremium(usuarioService.findById(id).getEmail()));
    }

    // =========================================================================
    // 3. OPERAÇÕES EXCLUSIVAS DE ADMINISTRAÇÃO (ROLE ADMIN)
    // =========================================================================

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "[ADMIN] Listar todos os usuários com paginação e ordenação")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UsuarioResponse> findAll(@PageableDefault(size = 10, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return usuarioService.findAll(pageable)
                .map(UsuarioResponse::fromEntity);
    }

    @GetMapping("/by-email")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "[ADMIN] Buscar usuário por e-mail")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse findByEmail(@RequestParam String email) {
        return UsuarioResponse.fromEntity(usuarioService.findByEmail(email));
    }

    @GetMapping("/{id}/rede-cuidado")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "[ADMIN] Visualizar rede de cuidado do usuário por ID")
    @Tag(name = "Care Circle", description = "Gestão colaborativa de tutores e co-cuidadores do pet")
    @Tag(name = "Usuário", description = "Gerenciamento de usuários")
    @PreAuthorize("hasRole('ADMIN')")
    public RedeCuidadoResponse getRedeCuidado(@PathVariable Long id) {
        return usuarioPetService.montarRedeCuidado(id);
    }

    @PatchMapping("/{id}/role")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "[ADMIN] Alterar role do usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return UsuarioResponse.fromEntity(usuarioService.updateRole(id, request.role()));
    }
}
