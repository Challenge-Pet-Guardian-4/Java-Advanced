package fiap.com.br.petguardian.trilha.aula;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.trilha.aula.dto.AulaRequest;
import fiap.com.br.petguardian.trilha.modulo.Modulo;
import fiap.com.br.petguardian.trilha.modulo.ModuloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AulaService {

    private final AulaRepository aulaRepository;
    private final ModuloRepository moduloRepository;

    public Page<Aula> findAll(Pageable pageable) {
        return aulaRepository.findAll(pageable);
    }

    public List<Aula> findAllByModuloId(Long moduloId) {
        findModuloById(moduloId);
        return aulaRepository.findAllByModuloId(moduloId);
    }

    public Aula findById(Long id) {
        return findAulaById(id);
    }

    @Transactional
    public Aula create(AulaRequest request) {
        Modulo modulo = findModuloById(request.moduloId());
        return aulaRepository.save(request.toEntity(modulo));
    }

    @Transactional
    public Aula update(Long id, AulaRequest request) {
        Aula aula = findAulaById(id);
        Modulo modulo = findModuloById(request.moduloId());
        aplicarEm(aula, request, modulo);
        return aulaRepository.save(aula);
    }

    @Transactional
    public void delete(Long id) {
        findAulaById(id);
        aulaRepository.deleteById(id);
    }

    @Transactional
    public Aula concluir(Long id) {
        Aula aula = findAulaById(id);
        aula.setConcluida(true);
        return aulaRepository.save(aula);
    }

    @Transactional
    public Aula desmarcar(Long id) {
        Aula aula = findAulaById(id);
        aula.setConcluida(false);
        return aulaRepository.save(aula);
    }

    private Aula findAulaById(Long id) {
        return aulaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aula com id " + id + " nao encontrada."));
    }

    private Modulo findModuloById(Long moduloId) {
        return moduloRepository.findById(moduloId)
                .orElseThrow(() -> new ResourceNotFoundException("Modulo com id " + moduloId + " nao encontrado."));
    }

    private void aplicarEm(Aula aula, AulaRequest request, Modulo modulo) {
        aula.setNome(request.nome());
        aula.setDescricao(request.descricao());
        aula.setPontosAula(request.pontosAula());
        aula.setDificuldade(request.dificuldade());
        aula.setConteudo(request.conteudo());
        aula.setConcluida(request.concluida());
        aula.setModulo(modulo);
    }
}
