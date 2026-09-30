package fiap.com.br.petguardian.usuario;

import fiap.com.br.petguardian.endereco.Endereco;
import fiap.com.br.petguardian.endereco.EnderecoService;
import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.telefone.Telefone;
import fiap.com.br.petguardian.telefone.TelefoneRepository;
import fiap.com.br.petguardian.usuario.dto.RedeCuidadoResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import fiap.com.br.petguardian.usuario.dto.UsuarioRequest;
import fiap.com.br.petguardian.usuariopet.UsuarioPetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EnderecoService enderecoService;
    private final TelefoneRepository telefoneRepository;
    private final UsuarioPetService usuarioPetService;
    private final PasswordEncoder passwordEncoder;

    public Page<Usuario> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    public Page<Usuario> findByNome(String nome, Pageable pageable) {
        return usuarioRepository.findByNomeContainingIgnoreCase(nome, pageable);
    }

    public Usuario findById(Long id) {
        return findUsuarioById(id);
    }

    @Transactional
    public Usuario create(UsuarioRequest request) {
        if (UsuarioRole.ADMIN.name().equalsIgnoreCase(request.role().trim())) {
            throw new IllegalArgumentException("Cadastro público não permite perfil de administrador.");
        }
        Telefone telefone = telefoneRepository.save(request.toTelefone());
        Endereco endereco = enderecoService.findOrCreateByCepAndNumero(request.endereco());
        return usuarioRepository.save(request.toEntity(telefone, endereco, passwordEncoder.encode(request.senha())));
    }

    @Transactional
    public Usuario update(Long id, UsuarioRequest request) {
        Usuario usuario = findUsuarioById(id);
        aplicarEm(usuario, request, passwordEncoder.encode(request.senha()));
        usuario.setEndereco(enderecoService.findOrCreateByCepAndNumero(request.endereco()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void delete(Long id) {
        findUsuarioById(id);
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public Usuario updateRole(Long id, String role) {
        Usuario usuario = findUsuarioById(id);
        usuario.setRole(UsuarioRole.valueOf(role.trim().toUpperCase()));
        return usuarioRepository.save(usuario);
    }

    public boolean isOwner(Long id, String email) {
        return usuarioRepository.existsByIdAndEmailIgnoreCase(id, email.trim());
    }

    public RedeCuidadoResponse getRedeCuidado(Long usuarioId) {
        return usuarioPetService.montarRedeCuidado(usuarioId);
    }

    public Usuario findUsuarioByEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com email " + email + " nao encontrado."));
    }

    private Usuario findUsuarioById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com id " + id + " nao encontrado."));
    }

    private void aplicarEm(Usuario usuario, UsuarioRequest request, String senhaCodificada) {
        usuario.setNome(request.nome());
        usuario.setEmail(request.email().trim().toLowerCase());
        usuario.setSenha(senhaCodificada);
        usuario.getTelefone().setDdd(request.ddd().trim());
        usuario.getTelefone().setNumero(request.numeroTelefone().trim());
    }
}
