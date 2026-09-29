package fiap.com.br.petguardian.auth;

import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.usuario.UsuarioRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Deve carregar UserDetails com sucesso para usuario existente")
    void deveCarregarUserDetailsSucesso() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Enzo Silva")
                .email("enzo@fiap.com.br")
                .senha("$2a$10$encodedPassword")
                .role(UsuarioRole.PREMIUM)
                .build();

        when(usuarioRepository.findByEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = authService.loadUserByUsername("enzo@fiap.com.br");

        assertNotNull(userDetails);
        assertEquals("enzo@fiap.com.br", userDetails.getUsername());
        assertEquals("$2a$10$encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PREMIUM")));
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não existir")
    void deveLancarExcecaoUsuarioNaoEncontrado() {
        when(usuarioRepository.findByEmailIgnoreCase("inexistente@fiap.com.br")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.loadUserByUsername("inexistente@fiap.com.br"));
    }
}
