package fiap.com.br.petguardian.usuariopet;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetRepository;
import fiap.com.br.petguardian.tarefa.TarefaRepository;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.usuario.dto.RedeCuidadoResponse;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorRequest;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorResponse;
import fiap.com.br.petguardian.usuariopet.dto.TransferirResponsabilidadeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioPetService {

    private final UsuarioPetRepository usuarioPetRepository;
    private final UsuarioRepository usuarioRepository;
    private final PetRepository petRepository;
    private final TarefaRepository tarefaRepository;
    private final RedeCuidadoMapper redeCuidadoMapper;

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
    public UsuarioPet vincularPrimeiroResponsavelPrincipal(Usuario usuario, Pet pet) {
        UsuarioPet vinculo = new UsuarioPet(new UsuarioPetId(usuario.getId(), pet.getId()), usuario, pet, true);
        return usuarioPetRepository.save(vinculo);
    }

    @Transactional
    public CoCuidadorResponse convidarCoCuidador(Long petId, CoCuidadorRequest request) {
        Pet pet = findPetById(petId);
        validarUsuarioNaoVinculadoPorEmail(request.email(), petId);
        Usuario convidado = findUsuarioByEmail(request.email());

        return CoCuidadorResponse.fromEntity(usuarioPetRepository.save(request.toEntity(convidado, pet)));
    }

    @Transactional
    public void transferirResponsabilidadePrincipal(Long petId, TransferirResponsabilidadeRequest request, String authEmail) {
        if (authEmail.trim().equalsIgnoreCase(request.novoResponsavelEmail().trim())) {
            throw new IllegalArgumentException("O novo responsavel nao pode ser o mesmo que o responsavel atual.");
        }

        UsuarioPet novoResponsavel = findVinculoPorEmail(request.novoResponsavelEmail(), petId);
        usuarioPetRepository.transferirResponsavelPrincipalNoBanco(petId, novoResponsavel.getUsuario().getId());
    }

    @Transactional
    public void desvincularCuidador(Long petId, String cuidadorEmail, String solicitanteEmail) {
        UsuarioPet vinculo = findVinculoPorEmail(cuidadorEmail, petId);
        validarPermissaoDesvinculacao(vinculo, solicitanteEmail);
        usuarioPetRepository.delete(vinculo);
    }

    // =========================================================================
    // 4. CONSOLIDAÇÃO DA REDE DE CUIDADO
    // =========================================================================

    @Transactional(readOnly = true)
    public RedeCuidadoResponse montarRedeCuidado(String email) {
        Usuario usuario = findUsuarioByEmail(email);
        List<UsuarioPet> vinculos = usuarioPetRepository.findAllByUsuarioEmailIgnoreCase(email.trim());

        if (vinculos.isEmpty()) {
            return redeCuidadoMapper.toEmptyResponse(usuario);
        }

        List<Long> petIds = vinculos.stream().map(up -> up.getPet().getId()).toList();

        var pets = redeCuidadoMapper.toPetResumoList(vinculos, carregarMapaTarefasPorPet(petIds));
        var cuidadores = redeCuidadoMapper.toCuidadorResumoList(
                usuarioPetRepository.findAllByPetIdIn(petIds),
                usuario.getEmail()
        );

        Map<String, Object> resumoDb = usuarioPetRepository.obterResumoRedeCuidadoNoBanco(usuario.getId());

        return new RedeCuidadoResponse(
                usuario.getEmail(),
                usuario.getNome(),
                pets,
                cuidadores,
                getInt(resumoDb, "p_tarefas_pendentes"),
                getInt(resumoDb, "p_tarefas_concluidas"),
                getInt(resumoDb, "p_pontos_acumulados")
        );
    }

    @Transactional(readOnly = true)
    public RedeCuidadoResponse montarRedeCuidado(Long usuarioId) {
        Usuario usuario = findUsuarioById(usuarioId);
        return montarRedeCuidado(usuario.getEmail());
    }

    // =========================================================================
    // 5. HELPERS PRIVADOS DE VALIDAÇÃO E BUSCA
    // =========================================================================

    private Map<Long, List<Long>> carregarMapaTarefasPorPet(List<Long> petIds) {
        Map<Long, List<Long>> mapa = new HashMap<>();
        petIds.forEach(id -> mapa.put(id, new ArrayList<>()));
        tarefaRepository.findTarefaIdsByPetIdIn(petIds).forEach(row -> mapa.get((Long) row[0]).add((Long) row[1]));
        return mapa;
    }

    private Pet findPetById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet com id " + id + " nao encontrado."));
    }

    private Usuario findUsuarioById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com id " + id + " nao encontrado."));
    }

    private Usuario findUsuarioByEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com email " + email + " nao encontrado."));
    }

    private UsuarioPet findVinculoPorEmail(String email, Long petId) {
        return usuarioPetRepository.findByUsuarioEmailIgnoreCaseAndPetId(email.trim(), petId)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo nao encontrado entre o usuario e o pet informados."));
    }

    private void validarUsuarioNaoVinculadoPorEmail(String email, Long petId) {
        if (usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId(email.trim(), petId)) {
            throw new IllegalArgumentException("Usuario informado ja possui vinculo com este pet.");
        }
    }

    private void validarPermissaoDesvinculacao(UsuarioPet vinculo, String solicitanteEmail) {
        if (vinculo.isResponsavelPrincipal()) {
            throw new IllegalArgumentException("Nao e permitido desvincular o responsavel principal do pet sem antes transferir a titularidade.");
        }

        Long petId = vinculo.getPet().getId();
        boolean isProprioUsuario = vinculo.getUsuario().getEmail().equalsIgnoreCase(solicitanteEmail.trim());
        boolean isResponsavel = usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetIdAndResponsavelPrincipalTrue(solicitanteEmail.trim(), petId);

        if (!isProprioUsuario && !isResponsavel) {
            throw new IllegalArgumentException("Apenas o proprio cuidador ou o responsavel principal podem remover este vinculo.");
        }
    }

    private static int getInt(Map<String, Object> map, String key) {
        return map != null && map.get(key) instanceof Number n ? n.intValue() : 0;
    }
}
