package fiap.com.br.petguardian.trilha.aula.conteudo;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.trilha.aula.AulaService;
import fiap.com.br.petguardian.trilha.aula.conteudo.dto.ConteudoAulaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConteudoAulaService {

    private final ConteudoAulaRepository conteudoRepository;
    private final AulaService aulaService;

    public Page<ConteudoAula> findAll(Pageable pageable) {
        return conteudoRepository.findAll(pageable);
    }

    public ConteudoAula findByAulaId(Long aulaId) {
        aulaService.findAulaEntityById(aulaId);
        return findConteudoByAulaId(aulaId);
    }

    public ConteudoAula create(Long aulaId, ConteudoAulaRequest request) {
        aulaService.findAulaEntityById(aulaId);

        if (conteudoRepository.findByAulaId(aulaId).isPresent()) {
            throw new IllegalArgumentException("Já existe conteúdo cadastrado para a aula com id " + aulaId + ".");
        }

        return conteudoRepository.save(request.toEntity(aulaId));
    }

    public ConteudoAula update(Long aulaId, ConteudoAulaRequest request) {
        aulaService.findAulaEntityById(aulaId);
        ConteudoAula existente = findConteudoByAulaId(aulaId);

        ConteudoAula entity = request.toEntity(aulaId);
        entity.setId(existente.getId());

        return conteudoRepository.save(entity);
    }

    public void deleteByAulaId(Long aulaId) {
        aulaService.findAulaEntityById(aulaId);
        findConteudoByAulaId(aulaId);
        conteudoRepository.deleteByAulaId(aulaId);
    }

    private ConteudoAula findConteudoByAulaId(Long aulaId) {
        return conteudoRepository.findByAulaId(aulaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conteúdo da aula com id " + aulaId + " não encontrado."));
    }
}
