package fiap.com.br.petguardian.pet;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.pet.dto.PetHistoryResponse;
import fiap.com.br.petguardian.pet.dto.PetPontuacaoResponse;
import fiap.com.br.petguardian.pet.dto.PetRequest;
import fiap.com.br.petguardian.pet.raca.Raca;
import fiap.com.br.petguardian.pet.raca.RacaRepository;
import fiap.com.br.petguardian.tarefa.TarefaRepository;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.tarefa.dto.TarefaResponse;
import fiap.com.br.petguardian.trilha.aula.AulaRepository;
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

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioPetRepository usuarioPetRepository;
    private final RacaRepository racaRepository;
    private final TarefaRepository tarefaRepository;
    private final AulaRepository aulaRepository;

    public Page<Pet> findAll(Pageable pageable) {
        return petRepository.findAll(pageable);
    }

    public Page<Pet> findByUsuario(Long usuarioId, Pageable pageable) {
        findUsuarioById(usuarioId);
        return petRepository.findByUsuarioId(usuarioId, pageable);
    }

    public Page<Pet> findByEmail(String email, Pageable pageable) {
        return petRepository.findByUsuarioEmail(email.trim(), pageable);
    }

    public Pet findById(Long id) {
        return findPetById(id);
    }

    public boolean isResponsavelPrincipal(Long petId, String email) {
        return usuarioPetRepository.isResponsavelPrincipalPorEmail(email.trim(), petId);
    }

    public boolean isCuidadorDoPet(Long petId, String email) {
        return usuarioPetRepository.existsByUsuarioEmailAndPetId(email.trim(), petId);
    }

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

    public PetHistoryResponse getConsolidatedHistory(Long petId) {
        Pet pet = findPetById(petId);
        return new PetHistoryResponse(
                pet.getId(),
                pet.getNome(),
                tarefaRepository.findConcluidasByPetId(petId, EnumStatus.CONCLUIDO)
                        .stream()
                        .map(TarefaResponse::fromEntity)
                        .toList());
    }

    @Transactional(readOnly = true)
    public PetPontuacaoResponse calcularPontuacaoTotalPet(Long petId) {
        Pet pet = findPetById(petId);
        int pontosTarefas = tarefaRepository.calcularPontosTarefasPorPet(petId, EnumStatus.CONCLUIDO);
        int pontosAulas = aulaRepository.calcularPontosAulasConcluidasPorPet(petId);
        return new PetPontuacaoResponse(pet.getId(), pet.getNome(), pontosTarefas, pontosAulas, pontosTarefas + pontosAulas);
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
