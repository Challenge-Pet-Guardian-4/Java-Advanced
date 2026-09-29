package fiap.com.br.petguardian.usuario;

import fiap.com.br.petguardian.usuario.dto.RedeCuidadoResponse;
import fiap.com.br.petguardian.usuario.dto.UsuarioRequest;
import fiap.com.br.petguardian.usuario.dto.UsuarioResponse;
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
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuário", description = "Gerenciamento de usuários")
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos os usuários com paginação e ordenação")
    public Page<UsuarioResponse> findAll(@PageableDefault(size = 10, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return usuarioService.findAll(pageable)
                .map(UsuarioResponse::fromEntity);
    }

    @GetMapping("/by-nome")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar usuários por nome com paginação e ordenação")
    public Page<UsuarioResponse> findByNome(@RequestParam String nome, @PageableDefault(size = 10, page = 0, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return usuarioService.findByNome(nome, pageable)
                .map(UsuarioResponse::fromEntity);
    }

    @GetMapping("/by-email")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar usuário por e-mail")
    public UsuarioResponse findByEmail(@RequestParam String email) {
        return UsuarioResponse.fromEntity(usuarioService.findUsuarioByEmail(email));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar usuário por ID")
    public UsuarioResponse findById(@PathVariable Long id) {
        return UsuarioResponse.fromEntity(usuarioService.findById(id));
    }

    @GetMapping("/{id}/rede-cuidado")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Visualizar rede de cuidado do usuário (pets, co-cuidadores e tarefas agrupadas)")
    @Tag(name = "Care Circle", description = "Gestão colaborativa de tutores e co-cuidadores do pet")
    @Tag(name = "Usuário", description = "Gerenciamento de usuários")
    public RedeCuidadoResponse getRedeCuidado(@PathVariable Long id) {
        return usuarioService.getRedeCuidado(id);
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
    public UsuarioResponse update(@PathVariable Long id, @Valid @RequestBody UsuarioRequest usuarioRequest) {
        return UsuarioResponse.fromEntity(usuarioService.update(id, usuarioRequest));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar usuário")
    public void delete(@PathVariable Long id) {
        usuarioService.delete(id);
    }
}
