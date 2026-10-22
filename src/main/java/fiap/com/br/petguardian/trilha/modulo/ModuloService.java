package fiap.com.br.petguardian.trilha.modulo;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.trilha.Trilha;
import fiap.com.br.petguardian.trilha.TrilhaRepository;
import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAulaRepository;
import fiap.com.br.petguardian.trilha.modulo.dto.ModuloRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuloService {

    private final ModuloRepository moduloRepository;
    private final TrilhaRepository trilhaRepository;
    private final ConteudoAulaRepository conteudoRepository;

    @Transactional(readOnly = true)
    public Page<Modulo> findAll(Pageable pageable) {
        return moduloRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Modulo> findAllByTrilhaId(Long trilhaId) {
        findTrilhaById(trilhaId);
        return moduloRepository.findAllByTrilhaId(trilhaId);
    }

    @Transactional(readOnly = true)
    public Modulo findById(Long id) {
        return findModuloById(id);
    }

    @Transactional
    public Modulo create(ModuloRequest request) {
        Trilha trilha = findTrilhaById(request.trilhaId());
        return moduloRepository.save(request.toEntity(trilha));
    }

    @Transactional
    public Modulo update(Long id, ModuloRequest request) {
        Modulo modulo = findModuloById(id);
        Trilha trilha = findTrilhaById(request.trilhaId());
        aplicarEm(modulo, request, trilha);
        return moduloRepository.save(modulo);
    }

    @Transactional
    public void delete(Long id) {
        Modulo modulo = findModuloById(id);
        List<Long> aulaIds = modulo.getAulas().stream().map(Aula::getId).toList();
        moduloRepository.delete(modulo);
        conteudoRepository.deleteByAulaIdIn(aulaIds);
    }

    private Modulo findModuloById(Long id) {
        return moduloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo com id " + id + " não encontrado."));
    }

    private Trilha findTrilhaById(Long trilhaId) {
        return trilhaRepository.findById(trilhaId)
                .orElseThrow(() -> new ResourceNotFoundException("Trilha com id " + trilhaId + " não encontrada."));
    }

    private void aplicarEm(Modulo modulo, ModuloRequest request, Trilha trilha) {
        modulo.setNome(request.nome());
        modulo.setTempoConclusao(request.tempoConclusao());
        modulo.setDescricao(request.descricao());
        modulo.setTrilha(trilha);
    }
}
