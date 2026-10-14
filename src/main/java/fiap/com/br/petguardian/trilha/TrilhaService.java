package fiap.com.br.petguardian.trilha;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetRepository;
import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAula;
import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAulaRepository;
import fiap.com.br.petguardian.trilha.dto.TrilhaCompletaResponse;
import fiap.com.br.petguardian.trilha.dto.TrilhaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrilhaService {

    private final TrilhaRepository trilhaRepository;
    private final PetRepository petRepository;
    private final ConteudoAulaRepository conteudoRepository;

    @Transactional(readOnly = true)
    public Page<Trilha> findAll(Pageable pageable) {
        return trilhaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Trilha> findAllByPetId(Long petId) {
        findPetById(petId);
        return trilhaRepository.findAllByPetId(petId);
    }

    @Transactional(readOnly = true)
    public List<Trilha> findByUsuarioEmail(String email) {
        return trilhaRepository.findAllByUsuarioEmail(email.trim());
    }


    @Transactional(readOnly = true)
    public List<TrilhaCompletaResponse> findTrilhasCompletasByUsuarioEmail(String email) {
        List<Trilha> trilhas = trilhaRepository.findAllByUsuarioEmail(email.trim());
        List<Long> aulaIds = trilhas.stream()
                .filter(t -> t.getModulos() != null)
                .flatMap(t -> t.getModulos().stream())
                .filter(m -> m.getAulas() != null)
                .flatMap(m -> m.getAulas().stream())
                .map(Aula::getId)
                .toList();

        Map<Long, String> conteudos = buscarConteudosEmLote(aulaIds);
        return trilhas.stream()
                .map(t -> TrilhaCompletaResponse.fromEntity(t, conteudos::get))
                .toList();
    }

    private Map<Long, String> buscarConteudosEmLote(Collection<Long> aulaIds) {
        if (aulaIds == null || aulaIds.isEmpty()) {
            return Map.of();
        }
        try {
            return conteudoRepository.findByAulaIdIn(aulaIds).stream()
                    .filter(c -> c.getCorpoMarkdown() != null)
                    .collect(Collectors.toMap(ConteudoAula::getAulaId, ConteudoAula::getCorpoMarkdown, (a, b) -> a));
        } catch (Exception e) {
            return Map.of();
        }
    }

    @Transactional(readOnly = true)
    public Trilha findById(Long id) {
        return findTrilhaById(id);
    }

    @Transactional
    public Trilha create(TrilhaRequest request) {
        Pet pet = findPetById(request.petId());
        return trilhaRepository.save(request.toEntity(pet));
    }

    @Transactional
    public Trilha update(Long id, TrilhaRequest request) {
        Trilha trilha = findTrilhaById(id);
        Pet pet = findPetById(request.petId());
        aplicarEm(trilha, request, pet);
        return trilhaRepository.save(trilha);
    }

    @Transactional
    public void delete(Long id) {
        findTrilhaById(id);
        trilhaRepository.deleteById(id);
    }

    private Trilha findTrilhaById(Long id) {
        return trilhaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trilha com id " + id + " não encontrada."));
    }

    private Pet findPetById(Long petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet com id " + petId + " não encontrado."));
    }

    private void aplicarEm(Trilha trilha, TrilhaRequest request, Pet pet) {
        trilha.setNome(request.nome());
        trilha.setDescricao(request.descricao());
        trilha.setPet(pet);
    }
}
