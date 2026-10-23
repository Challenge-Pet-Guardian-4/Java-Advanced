package fiap.com.br.petguardian.usuariopet;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetRepository;
import fiap.com.br.petguardian.tarefa.TarefaRepository;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorRequest;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorResponse;
import fiap.com.br.petguardian.usuariopet.dto.RedeCuidadoResponse;
import fiap.com.br.petguardian.usuariopet.dto.TransferirResponsabilidadeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioPetService {

    private final UsuarioPetRepository usuarioPetRepository;
    private final UsuarioRepository usuarioRepository;
    private final PetRepository petRepository;
    private final TarefaRepository tarefaRepository;

    // =========================================================================
    // 1. CHECAGEM DE AUTORIZAÇÃO E SEGURANÇA (SpEL)
    // =========================================================================

    @Transactional(readOnly = true)
    public boolean isResponsavelPrincipal(Long petId, String email) {
        return usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetIdAndResponsavelPrincipalTrue(email.trim(), petId);
    }

    @Transactional(readOnly = true)
    public boolean isCuidadorDoPet(Long petId, String email) {
        return usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId(email.trim(), petId);
    }

    // =========================================================================
    // 2. CONSULTAS DO CARE CIRCLE
    // =========================================================================

    @Transactional(readOnly = true)
    public List<CoCuidadorResponse> listarCuidadoresDoPet(Long petId) {
        findPetById(petId);
        return usuarioPetRepository.findAllByPetId(petId)
                .stream()
                .map(CoCuidadorResponse::fromEntity)
                .toList();
    }

    // =========================================================================
    // 3. GESTÃO DE VÍNCULOS E TITULARIDADE (CARE CIRCLE)
    // =========================================================================

    @Transactional
    public CoCuidadorResponse convidarCoCuidador(Long petId, CoCuidadorRequest request) {
        Pet pet = findPetById(petId);
        validarUsuarioNaoVinculadoPorEmail(request.email(), petId);
        Usuario convidado = findUsuarioByEmail(request.email());

        return CoCuidadorResponse.fromEntity(usuarioPetRepository.save(request.toEntity(convidado, pet)));
    }

    @Transactional
    public void transferirResponsabilidadePrincipal(Long petId, TransferirResponsabilidadeRequest request) {
        UsuarioPet novoResponsavel = findVinculoPorEmail(request.novoResponsavelEmail(), petId);
        if (novoResponsavel.isResponsavelPrincipal()) {
            throw new IllegalArgumentException("O novo responsável já é o responsável principal do pet.");
        }
        usuarioPetRepository.transferirResponsavelPrincipalNoBanco(petId, novoResponsavel.getUsuario().getId());
    }

    @Transactional
    public void desvincularCuidador(Long petId, String cuidadorEmail) {
        UsuarioPet vinculo = findVinculoPorEmail(cuidadorEmail, petId);
        if (vinculo.isResponsavelPrincipal()) {
            throw new IllegalArgumentException("Não é permitido desvincular o responsável principal do pet sem antes transferir a titularidade.");
        }
        usuarioPetRepository.delete(vinculo);
    }

    // =========================================================================
    // 4. CONSOLIDAÇÃO DA REDE DE CUIDADO
    // =========================================================================

    @Transactional(readOnly = true)
    public RedeCuidadoResponse montarRedeCuidado(String email) {
        return processarRedeCuidado(findUsuarioByEmail(email));
    }

    @Transactional(readOnly = true)
    public RedeCuidadoResponse montarRedeCuidado(Long usuarioId) {
        return processarRedeCuidado(findUsuarioById(usuarioId));
    }

    private RedeCuidadoResponse processarRedeCuidado(Usuario usuario) {
        List<UsuarioPet> vinculos = usuarioPetRepository.findAllByUsuarioEmailIgnoreCase(usuario.getEmail().trim());
        if (vinculos.isEmpty()) {
            return RedeCuidadoResponse.empty(usuario);
        }

        List<Long> petIds = vinculos.stream().map(up -> up.getPet().getId()).toList();
        Map<Long, Integer> tarefasPorPet = tarefaRepository.countTarefasByPetIdIn(petIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).intValue()));

        return RedeCuidadoResponse.of(
                usuario,
                vinculos,
                tarefasPorPet,
                usuarioPetRepository.findAllByPetIdIn(petIds),
                usuarioPetRepository.obterResumoRedeCuidadoNoBanco(usuario.getId()),
                usuarioPetRepository.obterNivelUsuarioNoBanco(usuario.getId())
        );
    }

    // =========================================================================
    // 5. HELPERS PRIVADOS DE VALIDAÇÃO E BUSCA
    // =========================================================================

    private Pet findPetById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet com id " + id + " não encontrado."));
    }

    private Usuario findUsuarioById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com id " + id + " não encontrado."));
    }

    private Usuario findUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com e-mail " + email + " não encontrado."));
    }

    private UsuarioPet findVinculoPorEmail(String email, Long petId) {
        return usuarioPetRepository.findByUsuarioEmailIgnoreCaseAndPetId(email.trim(), petId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo não encontrado entre o usuário e o pet informados."));
    }

    private void validarUsuarioNaoVinculadoPorEmail(String email, Long petId) {
        if (usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId(email.trim(), petId)) {
            throw new IllegalArgumentException("Usuário informado já possui vínculo com este pet.");
        }
    }
}
