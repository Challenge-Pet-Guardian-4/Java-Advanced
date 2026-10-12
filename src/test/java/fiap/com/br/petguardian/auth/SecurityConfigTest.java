package fiap.com.br.petguardian.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    @DisplayName("Deve converter authorities com prefixo ROLE_ a partir da claim role do token JWT")
    void deveConverterAuthoritiesDaClaimDoToken() {
        var jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .subject("enzo@fiap.com.br")
                .claim("role", "PREMIUM")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        var converter = securityConfig.jwtAuthenticationConverter();
        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertNotNull(auth);
        Collection<GrantedAuthority> authorities = auth.getAuthorities();
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PREMIUM")));
        assertEquals(1, authorities.stream().filter(a -> a.getAuthority().startsWith("ROLE_")).count());
    }
}
