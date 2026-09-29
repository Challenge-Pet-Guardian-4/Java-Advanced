package fiap.com.br.petguardian.usuariopet;

import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorRequest;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorResponse;
import fiap.com.br.petguardian.usuariopet.dto.TransferirResponsabilidadeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pets/{petId}")
@RequiredArgsConstructor
@Tag(name = "UsuarioPet", description = "Gestão de vínculos entre usuários e pets")
@Tag(name = "Care Circle", description = "Gestão colaborativa de tutores e co-cuidadores do pet")
public class UsuarioPetController {

    private final UsuarioPetService usuarioPetService;

    @GetMapping("/cuidadores")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos os cuidadores vinculados a um pet")
    public List<CoCuidadorResponse> listarCuidadores(@PathVariable Long petId) {
        return usuarioPetService.listarCuidadoresDoPet(petId);
    }

    @PostMapping("/cuidadores")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Convidar um novo co-cuidador para o pet (por ID ou e-mail)")
    public CoCuidadorResponse convidarCuidador(
            @PathVariable Long petId,
            @Valid @RequestBody CoCuidadorRequest request
    ) {
        return usuarioPetService.convidarCoCuidador(petId, request);
    }

    @DeleteMapping("/cuidadores/{usuarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desvincular um co-cuidador do pet")
    public void desvincularCuidador(
            @PathVariable Long petId,
            @PathVariable Long usuarioId,
            @RequestParam Long solicitanteId
    ) {
        usuarioPetService.desvincularCuidador(petId, usuarioId, solicitanteId);
    }

    @PatchMapping("/responsavel-principal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Transferir a titularidade de responsavel principal para outro co-cuidador")
    public void transferirResponsavelPrincipal(
            @PathVariable Long petId,
            @Valid @RequestBody TransferirResponsabilidadeRequest request
    ) {
        usuarioPetService.transferirResponsabilidadePrincipal(petId, request);
    }
}
