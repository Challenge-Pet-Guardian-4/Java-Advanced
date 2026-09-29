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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TokenService tokenService;

    @Test
    @DisplayName("Deve gerar token JWT com role e subject corretos")
    void deveGerarTokenComSucesso() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("enzo@fiap.com.br")
                .role(UsuarioRole.PREMIUM)
                .build();

        Jwt mockJwt = new Jwt(
                "mocked.jwt.token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                java.util.Map.of("alg", "RS256"),
                java.util.Map.of("sub", "enzo@fiap.com.br", "role", "PREMIUM")
        );

        when(usuarioRepository.findByEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        String token = tokenService.generateToken("enzo@fiap.com.br");

        assertNotNull(token);
        assertEquals("mocked.jwt.token", token);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException ao tentar gerar token para email inexistente")
    void deveLancarExcecaoEmailInexistente() {
        when(usuarioRepository.findByEmailIgnoreCase("naoexiste@fiap.com.br")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> tokenService.generateToken("naoexiste@fiap.com.br"));
    }
}
