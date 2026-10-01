package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.usuariopet.UsuarioPet;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioPetValidator {

    private final UsuarioPetRepository usuarioPetRepository;

    public void validarUsuarioNaoVinculadoPorEmail(String email, Long petId) {
        if (usuarioPetRepository.existsByUsuarioEmailAndPetId(email.trim(), petId)) {
            throw new IllegalArgumentException("Usuario informado ja possui vinculo com este pet.");
        }
    }

    public void validarPermissaoDesvinculacao(UsuarioPet vinculo, String solicitanteEmail) {
        if (vinculo.isResponsavelPrincipal()) {
            throw new IllegalArgumentException("Nao e permitido desvincular o responsavel principal do pet sem antes transferir a titularidade.");
        }

        Long petId = vinculo.getPet().getId();
        boolean isProprioUsuario = vinculo.getUsuario().getEmail().equalsIgnoreCase(solicitanteEmail.trim());
        boolean isResponsavel = usuarioPetRepository.isResponsavelPrincipalPorEmail(solicitanteEmail.trim(), petId);

        if (!isProprioUsuario && !isResponsavel) {
            throw new IllegalArgumentException("Apenas o proprio cuidador ou o responsavel principal podem remover este vinculo.");
        }
    }
}
