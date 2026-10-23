package fiap.com.br.petguardian.trilha.aula;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAula;
import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAulaRepository;
import fiap.com.br.petguardian.trilha.aula.dto.AulaRequest;
import fiap.com.br.petguardian.trilha.aula.dto.AulaResponse;
import fiap.com.br.petguardian.trilha.modulo.Modulo;
import fiap.com.br.petguardian.trilha.modulo.ModuloRepository;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.usuario.UsuarioRole;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AulaService {

    private final AulaRepository aulaRepository;
    private final ModuloRepository moduloRepository;
    private final UsuarioPetRepository usuarioPetRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConteudoAulaRepository conteudoRepository;

    @Transactional(readOnly = true)
    public boolean isCuidadorDaAula(Long aulaId, String email) {
        Aula aula = findAulaById(aulaId);
        Long petId = aula.getModulo().getTrilha().getPet().getId();
        return usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId(email.trim(), petId);
    }

    @Transactional(readOnly = true)
    public Page<AulaResponse> findAll(Pageable pageable) {
        Page<Aula> aulas = aulaRepository.findAll(pageable);
        Map<Long, String> conteudos = buscarConteudosEmLote(aulas.map(Aula::getId).toList());
        return aulas.map(aula -> AulaResponse.fromEntity(aula, conteudos.get(aula.getId())));
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> findAllByModuloId(Long moduloId) {
        findModuloById(moduloId);
        List<Aula> aulas = aulaRepository.findAllByModuloId(moduloId);
        Map<Long, String> conteudos = buscarConteudosEmLote(aulas.stream().map(Aula::getId).toList());
        return aulas.stream()
                .map(aula -> AulaResponse.fromEntity(aula, conteudos.get(aula.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public AulaResponse findById(Long id) {
        Aula aula = findAulaById(id);
        return AulaResponse.fromEntity(aula, buscarConteudoMarkdown(id));
    }

    @Transactional
    public AulaResponse create(AulaRequest request) {
        Modulo modulo = findModuloById(request.moduloId());
        Aula aulaSalva = aulaRepository.save(request.toEntity(modulo));
        return AulaResponse.fromEntity(aulaSalva, null);
    }

    @Transactional
    public AulaResponse update(Long id, AulaRequest request) {
        Aula aula = findAulaById(id);
        Modulo modulo = findModuloById(request.moduloId());
        aplicarEm(aula, request, modulo);
        Aula aulaAtualizada = aulaRepository.save(aula);
        return AulaResponse.fromEntity(aulaAtualizada, buscarConteudoMarkdown(id));
    }

    @Transactional
    public void delete(Long id) {
        findAulaById(id);
        aulaRepository.deleteById(id);
        conteudoRepository.deleteByAulaId(id);
    }

    @Transactional
    public AulaResponse concluir(Long id, String authEmail) {
        Aula aula = findAulaById(id);
        validarCuidadorDaAula(aula, authEmail);
        aula.setConcluida(true);
        Aula salva = aulaRepository.save(aula);
        return AulaResponse.fromEntity(salva, buscarConteudoMarkdown(id));
    }

    @Transactional
    public AulaResponse desmarcar(Long id, String authEmail) {
        Aula aula = findAulaById(id);
        validarCuidadorDaAula(aula, authEmail);
        aula.setConcluida(false);
        Aula salva = aulaRepository.save(aula);
        return AulaResponse.fromEntity(salva, buscarConteudoMarkdown(id));
    }

    public String buscarConteudoMarkdown(Long aulaId) {
        try {
            return conteudoRepository.findByAulaId(aulaId)
                    .map(ConteudoAula::getCorpoMarkdown)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Falha ao consultar o conteúdo NoSQL da aula {}: {}", aulaId, e.getMessage());
            return null;
        }
    }

    public Map<Long, String> buscarConteudosEmLote(Collection<Long> aulaIds) {
        if (aulaIds == null || aulaIds.isEmpty()) {
            return Map.of();
        }
        try {
            return conteudoRepository.findByAulaIdIn(aulaIds).stream()
                    .filter(c -> c.getCorpoMarkdown() != null)
                    .collect(Collectors.toMap(ConteudoAula::getAulaId, ConteudoAula::getCorpoMarkdown, (a, b) -> a));
        } catch (Exception e) {
            log.warn("Falha ao consultar conteúdos NoSQL em lote: {}", e.getMessage());
            return Map.of();
        }
    }

    private Aula findAulaById(Long id) {
        return aulaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aula com id " + id + " não encontrada."));
    }

    private Modulo findModuloById(Long moduloId) {
        return moduloRepository.findById(moduloId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo com id " + moduloId + " não encontrado."));
    }

    private void aplicarEm(Aula aula, AulaRequest request, Modulo modulo) {
        aula.setNome(request.nome());
        aula.setDescricao(request.descricao());
        aula.setPontosAula(request.pontosAula());
        aula.setDificuldade(DificuldadeAula.valueOf(request.dificuldade().trim().toUpperCase()));
        aula.setConcluida(request.concluida());
        aula.setModulo(modulo);
    }

    private void validarCuidadorDaAula(Aula aula, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com e-mail " + email + " não encontrado."));
        if (usuario.getRole() == UsuarioRole.ADMIN) {
            return;
        }
        Long petId = aula.getModulo().getTrilha().getPet().getId();
        if (!usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId(email.trim(), petId)) {
            throw new IllegalArgumentException("Usuário informado não está vinculado ao pet da trilha.");
        }
    }
}
