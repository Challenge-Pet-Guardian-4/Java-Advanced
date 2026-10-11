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
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private SecurityConfig securityConfig;

    private Jwt criarJwtMock(String subject) {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    @DisplayName("Deve converter authorities com prefixo ROLE_ consultando a role atual no banco")
    void deveConverterAuthoritiesDoBanco() {
        var jwt = criarJwtMock("enzo@fiap.com.br");
        var usuario = Usuario.builder()
                .id(1L)
                .email("enzo@fiap.com.br")
                .role(UsuarioRole.PREMIUM)
                .build();

        when(usuarioRepository.findByEmailIgnoreCase("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));

        var converter = securityConfig.jwtAuthenticationConverter(usuarioRepository);
        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertNotNull(auth);
        Collection<GrantedAuthority> authorities = auth.getAuthorities();
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PREMIUM")));
        assertEquals(1, authorities.stream().filter(a -> a.getAuthority().startsWith("ROLE_")).count());
    }

    @Test
    @DisplayName("Deve retornar lista vazia de authorities quando usuario nao for encontrado no banco")
    void deveRetornarListaVaziaQuandoUsuarioNaoEncontrado() {
        var jwt = criarJwtMock("inexistente@fiap.com.br");

        when(usuarioRepository.findByEmailIgnoreCase("inexistente@fiap.com.br")).thenReturn(Optional.empty());

        var converter = securityConfig.jwtAuthenticationConverter(usuarioRepository);
        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().startsWith("ROLE_")));
    }
}
