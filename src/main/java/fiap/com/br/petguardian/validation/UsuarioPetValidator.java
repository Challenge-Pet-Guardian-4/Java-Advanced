package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.usuariopet.UsuarioPet;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UsuarioPetValidator {

    private final UsuarioPetRepository usuarioPetRepository;

    public void validarResponsavelPrincipal(Long usuarioId, Long petId) {
        if (!usuarioPetRepository.isResponsavelPrincipal(usuarioId, petId)) {
            throw new IllegalArgumentException("Somente o responsavel principal tem permissao para esta operacao neste pet.");
        }
    }

    public void validarUsuarioNaoVinculado(Long usuarioId, Long petId) {
        if (usuarioPetRepository.existsByUsuarioIdAndPetId(usuarioId, petId)) {
            throw new IllegalArgumentException("Usuario informado ja possui vinculo com este pet.");
        }
    }

    public void validarPermissaoDesvinculacao(UsuarioPet vinculo, Long solicitanteId) {
        if (vinculo.isResponsavelPrincipal()) {
            throw new IllegalArgumentException("Nao e permitido desvincular o responsavel principal do pet sem antes transferir a titularidade.");
        }

        Long usuarioId = vinculo.getUsuario().getId();
        Long petId = vinculo.getPet().getId();

        boolean isProprioUsuario = Objects.equals(usuarioId, solicitanteId);
        boolean isResponsavel = usuarioPetRepository.isResponsavelPrincipal(solicitanteId, petId);

        if (!isProprioUsuario && !isResponsavel) {
            throw new IllegalArgumentException("Apenas o proprio cuidador ou o responsavel principal podem remover este vinculo.");
        }
    }
}
