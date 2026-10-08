package fiap.com.br.petguardian.trilha.aula.conteudo;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.aula.AulaRepository;
import fiap.com.br.petguardian.trilha.aula.conteudo.dto.ConteudoAulaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConteudoAulaService {

    private final ConteudoAulaRepository conteudoRepository;
    private final AulaRepository aulaRepository;

    public Page<ConteudoAula> findAll(Pageable pageable) {
        return conteudoRepository.findAll(pageable);
    }

    public ConteudoAula findByAulaId(Long aulaId) {
        findAulaById(aulaId);
        return findConteudoByAulaId(aulaId);
    }

    public ConteudoAula create(Long aulaId, ConteudoAulaRequest request) {
        findAulaById(aulaId);

        if (conteudoRepository.findByAulaId(aulaId).isPresent()) {
            throw new IllegalArgumentException("Ja existe conteudo cadastrado para a aula com id " + aulaId + ".");
        }

        return conteudoRepository.save(request.toEntity(aulaId));
    }

    public ConteudoAula update(Long aulaId, ConteudoAulaRequest request) {
        findAulaById(aulaId);
        ConteudoAula existente = findConteudoByAulaId(aulaId);

        ConteudoAula entity = request.toEntity(aulaId);
        entity.setId(existente.getId());

        return conteudoRepository.save(entity);
    }

    public void deleteByAulaId(Long aulaId) {
        findAulaById(aulaId);
        findConteudoByAulaId(aulaId);
        conteudoRepository.deleteByAulaId(aulaId);
    }

    private Aula findAulaById(Long aulaId) {
        return aulaRepository.findById(aulaId)
                .orElseThrow(() -> new ResourceNotFoundException("Aula com id " + aulaId + " nao encontrada."));
    }

    private ConteudoAula findConteudoByAulaId(Long aulaId) {
        return conteudoRepository.findByAulaId(aulaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conteudo da aula com id " + aulaId + " nao encontrado."));
    }
}
