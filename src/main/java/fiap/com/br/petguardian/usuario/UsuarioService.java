package fiap.com.br.petguardian.usuario;

import fiap.com.br.petguardian.endereco.Endereco;
import fiap.com.br.petguardian.endereco.EnderecoService;
import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.pet.PetService;
import fiap.com.br.petguardian.telefone.Telefone;
import fiap.com.br.petguardian.telefone.TelefoneRepository;
import fiap.com.br.petguardian.usuario.dto.UsuarioRequest;
import fiap.com.br.petguardian.usuariopet.UsuarioPet;
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
    private final PetService petService;

    public Page<Usuario> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    public Usuario findById(Long id) {
        return findUsuarioById(id);
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com e-mail " + email + " não encontrado."));
    }

    @Transactional
    public Usuario create(UsuarioRequest request) {
        Endereco endereco = enderecoService.findOrCreateByCepAndNumero(request.endereco());
        Telefone telefone = telefoneRepository.save(request.toTelefone());
        return usuarioRepository.save(request.toEntity(telefone, endereco, passwordEncoder.encode(request.senha())));
    }

    @Transactional
    public Usuario update(Long id, UsuarioRequest request) {
        Usuario usuario = findUsuarioById(id);
        aplicarEm(usuario, request);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario update(String email, UsuarioRequest request) {
        Usuario usuario = findByEmail(email);
        aplicarEm(usuario, request);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario upgradePremium(String email) {
        Usuario usuario = findByEmail(email);
        if (usuario.getRole() != UsuarioRole.COMUM) {
            throw new IllegalArgumentException("Somente usuários com perfil COMUM podem realizar upgrade para PREMIUM.");
        }
        usuario.setRole(UsuarioRole.PREMIUM);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario updateRole(Long id, String role) {
        Usuario usuario = findUsuarioById(id);
        UsuarioRole novoRole = UsuarioRole.valueOf(role.trim().toUpperCase());
        if (usuario.getRole() == UsuarioRole.ADMIN && usuarioRepository.countByRole(UsuarioRole.ADMIN) <= 1) {
            throw new IllegalArgumentException("Não é possível rebaixar o único administrador do sistema.");
        }
        usuario.setRole(novoRole);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void delete(Long id) {
        deleteUsuario(findUsuarioById(id));
    }

    @Transactional
    public void delete(String email) {
        deleteUsuario(findByEmail(email));
    }

    private void deleteUsuario(Usuario usuario) {
        if (usuario.getRole() == UsuarioRole.ADMIN && usuarioRepository.countByRole(UsuarioRole.ADMIN) <= 1) {
            throw new IllegalArgumentException("Não é possível excluir o único administrador do sistema.");
        }
        usuario.getUsuarioPets().stream()
                .filter(UsuarioPet::isResponsavelPrincipal)
                .map(UsuarioPet::getPet)
                .toList()
                .forEach(pet -> petService.delete(pet.getId()));

        usuarioRepository.delete(usuario);
    }

    private Usuario findUsuarioById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com id " + id + " não encontrado."));
    }

    private void aplicarEm(Usuario usuario, UsuarioRequest request) {
        usuario.setNome(request.nome());
        usuario.setEmail(request.email().trim().toLowerCase());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.getTelefone().setDdd(request.ddd().trim());
        usuario.getTelefone().setNumero(request.numeroTelefone().trim());
        usuario.setEndereco(enderecoService.findOrCreateByCepAndNumero(request.endereco()));
    }
}
