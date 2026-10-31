package fiap.com.br.petguardian.trilha;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetService;
import fiap.com.br.petguardian.trilha.aula.AulaService;
import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAulaRepository;
import fiap.com.br.petguardian.trilha.dto.TrilhaCompletaResponse;
import fiap.com.br.petguardian.trilha.dto.TrilhaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrilhaService {

    private final TrilhaRepository trilhaRepository;
    private final PetService petService;
    private final AulaService aulaService;
    private final ConteudoAulaRepository conteudoRepository;

    @Transactional(readOnly = true)
    public Page<Trilha> findAll(Pageable pageable) {
        return trilhaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Trilha> findAllByPetId(Long petId) {
        petService.findById(petId);
        return trilhaRepository.findAllByPetId(petId);
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

        Map<Long, String> conteudos = aulaService.buscarConteudosEmLote(aulaIds);
        return trilhas.stream()
                .map(t -> TrilhaCompletaResponse.fromEntity(t, conteudos::get))
                .toList();
    }


    @Transactional(readOnly = true)
    public Trilha findById(Long id) {
        return findTrilhaById(id);
    }

    @Transactional
    public Trilha create(TrilhaRequest request) {
        Pet pet = petService.findById(request.petId());
        return trilhaRepository.save(request.toEntity(pet));
    }

    @Transactional
    public Trilha update(Long id, TrilhaRequest request) {
        Trilha trilha = findTrilhaById(id);
        Pet pet = petService.findById(request.petId());
        aplicarEm(trilha, request, pet);
        return trilhaRepository.save(trilha);
    }

    @Transactional
    public void delete(Long id) {
        Trilha trilha = findTrilhaById(id);
        List<Long> aulaIds = trilha.getModulos().stream()
                .flatMap(m -> m.getAulas().stream())
                .map(Aula::getId)
                .toList();
        trilhaRepository.delete(trilha);
        conteudoRepository.deleteByAulaIdIn(aulaIds);
    }

    private Trilha findTrilhaById(Long id) {
        return trilhaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trilha com id " + id + " não encontrada."));
    }


    private void aplicarEm(Trilha trilha, TrilhaRequest request, Pet pet) {
        trilha.setNome(request.nome());
        trilha.setDescricao(request.descricao());
        trilha.setPet(pet);
    }
}
