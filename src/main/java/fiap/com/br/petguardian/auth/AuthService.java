package fiap.com.br.petguardian.auth;

import fiap.com.br.petguardian.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow(
                () -> new UsernameNotFoundException("Usuário não encontrado: " + email)
        );

        return User
                .withUsername(user.getEmail())
                .password(user.getSenha())
                .roles(user.getRole().name())
                .build();
    }
}
