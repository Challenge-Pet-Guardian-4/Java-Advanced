package fiap.com.br.petguardian.usuario;

import fiap.com.br.petguardian.endereco.Endereco;
import fiap.com.br.petguardian.endereco.EnderecoService;
import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.telefone.Telefone;
import fiap.com.br.petguardian.telefone.TelefoneRepository;
import fiap.com.br.petguardian.usuario.dto.UsuarioRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EnderecoService enderecoService;
    private final TelefoneRepository telefoneRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<Usuario> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com id " + id + " nao encontrado."));
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com email " + email + " nao encontrado."));
    }

    @Transactional
    public Usuario create(UsuarioRequest request) {
        Telefone telefone = telefoneRepository.save(request.toTelefone());
        Endereco endereco = enderecoService.findOrCreateByCepAndNumero(request.endereco());
        return usuarioRepository.save(request.toEntity(telefone, endereco, passwordEncoder.encode(request.senha())));
    }

    @Transactional
    public Usuario update(Long id, UsuarioRequest request) {
        return atualizarDados(findById(id), request);
    }

    @Transactional
    public Usuario update(String email, UsuarioRequest request) {
        return atualizarDados(findByEmail(email), request);
    }

    @Transactional
    public Usuario upgradePremium(Long id) {
        return executarUpgradePremium(findById(id));
    }

    @Transactional
    public Usuario upgradePremium(String email) {
        return executarUpgradePremium(findByEmail(email));
    }

    @Transactional
    public Usuario updateRole(Long id, String role) {
        Usuario usuario = findById(id);
        usuario.setRole(UsuarioRole.valueOf(role.trim().toUpperCase()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        usuarioRepository.deleteById(id);
    }

    public boolean isOwner(Long id, String email) {
        return usuarioRepository.existsByIdAndEmailIgnoreCase(id, email.trim());
    }

    private Usuario atualizarDados(Usuario usuario, UsuarioRequest request) {
        usuario.setNome(request.nome());
        usuario.setEmail(request.email().trim().toLowerCase());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.getTelefone().setDdd(request.ddd().trim());
        usuario.getTelefone().setNumero(request.numeroTelefone().trim());
        usuario.setEndereco(enderecoService.findOrCreateByCepAndNumero(request.endereco()));
        return usuarioRepository.save(usuario);
    }

    private Usuario executarUpgradePremium(Usuario usuario) {
        if (usuario.getRole() != UsuarioRole.COMUM) {
            throw new IllegalArgumentException("Somente usuários com perfil COMUM podem realizar upgrade para PREMIUM.");
        }
        usuario.setRole(UsuarioRole.PREMIUM);
        return usuarioRepository.save(usuario);
    }
}
