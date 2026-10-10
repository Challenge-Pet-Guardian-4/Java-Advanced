package fiap.com.br.petguardian.pet;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.pet.dto.PetDetailResponse;
import fiap.com.br.petguardian.pet.dto.PetPontuacaoAgregadaResponse;
import fiap.com.br.petguardian.pet.dto.PetPontuacaoResponse;
import fiap.com.br.petguardian.pet.dto.PetRequest;
import fiap.com.br.petguardian.pet.historico.HistoricoRepository;
import fiap.com.br.petguardian.pet.raca.Raca;
import fiap.com.br.petguardian.pet.raca.RacaRepository;
import fiap.com.br.petguardian.tarefa.TarefaRepository;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.usuariopet.UsuarioPet;
import fiap.com.br.petguardian.usuariopet.UsuarioPetId;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioPetRepository usuarioPetRepository;
    private final RacaRepository racaRepository;
    private final TarefaRepository tarefaRepository;
    private final HistoricoRepository historicoRepository;

    // =========================================================================
    // 1. CHECAGEM DE AUTORIZAÇÃO E SEGURANÇA (SpEL)
    // =========================================================================

    public boolean isResponsavelPrincipal(Long petId, String email) {
        return usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetIdAndResponsavelPrincipalTrue(email.trim(), petId);
    }

    public boolean isCuidadorDoPet(Long petId, String email) {
        return usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId(email.trim(), petId);
    }

    // =========================================================================
    // 2. CONSULTAS GERAIS E POR USUÁRIO
    // =========================================================================

    public Page<Pet> findAll(Pageable pageable) {
        return petRepository.findAll(pageable);
    }

    public Page<Pet> findByUsuario(Long usuarioId, Pageable pageable) {
        findUsuarioById(usuarioId);
        return petRepository.findByUsuarioId(usuarioId, pageable);
    }

    public Page<Pet> findByEmail(String email, Pageable pageable) {
        Usuario usuario = findUsuarioByEmail(email);
        return findByUsuario(usuario.getId(), pageable);
    }

    public Pet findById(Long id) {
        return findPetById(id);
    }

    // =========================================================================
    // 3. CICLO DE VIDA DO PET (CRIAÇÃO, ATUALIZAÇÃO, EXCLUSÃO)
    // =========================================================================

    @Transactional
    public Pet create(PetRequest petRequest, String authEmail) {
        Usuario usuario = findUsuarioByEmail(authEmail);
        Raca raca = findOrCreateRaca(petRequest.raca());
        Pet petSalvo = petRepository.save(petRequest.toEntity(raca));

        UsuarioPet vinculo = new UsuarioPet(new UsuarioPetId(usuario.getId(), petSalvo.getId()), usuario, petSalvo, true);
        usuarioPetRepository.save(vinculo);
        return petSalvo;
    }

    @Transactional
    public Pet update(Long id, PetRequest petRequest) {
        Pet pet = findPetById(id);
        Raca raca = findOrCreateRaca(petRequest.raca());
        aplicarEm(pet, petRequest, raca);
        return petRepository.save(pet);
    }

    @Transactional
    public void delete(Long id) {
        findPetById(id);
        petRepository.deleteById(id);
    }

    // =========================================================================
    // 4. FICHA CONSOLIDADA DO PET (DETALHE COMPLETO)
    // =========================================================================

    @Transactional(readOnly = true)
    public PetDetailResponse getPetDetail(Long petId) {
        Pet pet = findPetById(petId);
        return PetDetailResponse.of(
                pet,
                calcularPontuacaoPet(pet),
                usuarioPetRepository.findAllByPetId(petId),
                tarefaRepository.findAllByPetIdInAndStatusNomeStatusOrderByConclusaoDesc(List.of(petId), EnumStatus.CONCLUIDO),
                historicoRepository.findAllByPetIdOrderByDataHistDesc(petId)
        );
    }

    // =========================================================================
    // 5. PONTUAÇÃO E PROCEDURES ORACLE (INDIVIDUAL E AGREGADA)
    // =========================================================================

    @Transactional(readOnly = true)
    public PetPontuacaoResponse calcularPontuacaoTotalPet(Long petId) {
        return calcularPontuacaoPet(findPetById(petId));
    }

    public PetPontuacaoResponse calcularPontuacaoPet(Pet pet) {
        Map<String, Object> out = petRepository.calcularPontuacaoPetNoBanco(pet.getId());
        return PetPontuacaoResponse.fromMap(pet.getId(), pet.getNome(), out);
    }

    @Transactional(readOnly = true)
    public PetPontuacaoAgregadaResponse calcularPontuacaoAgregadaPetsUsuario(String email) {
        Usuario usuario = findUsuarioByEmail(email);
        Map<String, Object> out = petRepository.calcularPontuacaoUsuarioNoBanco(usuario.getId());

        List<PetPontuacaoResponse> detalhePets = usuarioPetRepository.findAllByUsuarioEmailIgnoreCase(email.trim()).stream()
                .map(up -> calcularPontuacaoPet(up.getPet()))
                .toList();

        return PetPontuacaoAgregadaResponse.fromMap(out, detalhePets);
    }

    // =========================================================================
    // 6. HELPERS PRIVADOS (BUSCAS E VALIDAÇÕES)
    // =========================================================================

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

    private Raca findOrCreateRaca(String nomeRaca) {
        return racaRepository.findByNomeIgnoreCase(nomeRaca)
                .orElseGet(() -> racaRepository.save(Raca.builder().nome(nomeRaca).build()));
    }

    private void aplicarEm(Pet pet, PetRequest request, Raca raca) {
        pet.setNome(request.nome());
        pet.setDataNasc(request.dataNasc());
        pet.setRaca(raca);
        pet.setPorte(PetPorte.valueOf(request.porte().toUpperCase()));
        pet.setSexo(Character.toUpperCase(request.sexo()));
        pet.setCastrado(request.castrado());
    }
}
