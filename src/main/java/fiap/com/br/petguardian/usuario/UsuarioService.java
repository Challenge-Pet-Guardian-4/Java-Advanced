package fiap.com.br.petguardian.usuario;

import fiap.com.br.petguardian.endereco.EnderecoService;
import fiap.com.br.petguardian.exception.ResourceNotFoundException;
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
    public Usuario create(UsuarioRequest usuarioRequest) {
        Usuario usuario = usuarioRequest.toEntity(
                telefoneRepository.save(usuarioRequest.toTelefone()),
                usuarioRequest.email(),
                passwordEncoder.encode(usuarioRequest.senha())
        );
        usuario.getEnderecos().add(enderecoService.findOrCreateByCepAndNumero(usuarioRequest.endereco()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario update(Long id, UsuarioRequest usuarioRequest) {
        Usuario usuario = findUsuarioById(id);
        aplicarEm(usuario, usuarioRequest, passwordEncoder.encode(usuarioRequest.senha()));
        usuario.getEnderecos().add(enderecoService.findOrCreateByCepAndNumero(usuarioRequest.endereco()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void delete(Long id) {
        findUsuarioById(id);
        usuarioRepository.deleteById(id);
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
        usuario.setRole(UsuarioRole.valueOf(request.role().trim().toUpperCase()));
        if (usuario.getTelefone() != null) {
            usuario.getTelefone().setDdd(request.ddd().trim());
            usuario.getTelefone().setNumero(request.numeroTelefone().trim());
        }
    }
}
