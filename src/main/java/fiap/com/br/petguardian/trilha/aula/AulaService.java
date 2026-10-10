package fiap.com.br.petguardian.trilha.aula;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.trilha.aula.dto.AulaRequest;
import fiap.com.br.petguardian.trilha.modulo.Modulo;
import fiap.com.br.petguardian.trilha.modulo.ModuloRepository;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.usuario.UsuarioRole;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
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
    private final UsuarioPetRepository usuarioPetRepository;
    private final UsuarioRepository usuarioRepository;

    public boolean isCuidadorDaAula(Long aulaId, String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        Aula aula = findAulaById(aulaId);
        if (aula.getModulo() == null || aula.getModulo().getTrilha() == null || aula.getModulo().getTrilha().getPet() == null) {
            return false;
        }
        Long petId = aula.getModulo().getTrilha().getPet().getId();
        return usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId(email.trim(), petId);
    }

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
        return concluir(id, null);
    }

    @Transactional
    public Aula concluir(Long id, String authEmail) {
        Aula aula = findAulaById(id);
        if (authEmail != null && !authEmail.isBlank()) {
            validarCuidadorDaAula(aula, authEmail);
        }
        aula.setConcluida(true);
        return aulaRepository.save(aula);
    }

    @Transactional
    public Aula desmarcar(Long id) {
        return desmarcar(id, null);
    }

    @Transactional
    public Aula desmarcar(Long id, String authEmail) {
        Aula aula = findAulaById(id);
        if (authEmail != null && !authEmail.isBlank()) {
            validarCuidadorDaAula(aula, authEmail);
        }
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

    private void validarCuidadorDaAula(Aula aula, String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com email " + email + " nao encontrado."));
        if (usuario.getRole() == UsuarioRole.ADMIN) {
            return;
        }
        if (aula.getModulo() == null || aula.getModulo().getTrilha() == null || aula.getModulo().getTrilha().getPet() == null) {
            throw new IllegalArgumentException("Aula nao possui vinculo valido com pet dono da trilha.");
        }
        Long petId = aula.getModulo().getTrilha().getPet().getId();
        if (!usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId(email.trim(), petId)) {
            throw new IllegalArgumentException("Usuario informado nao esta vinculado ao pet da trilha.");
        }
    }
}
