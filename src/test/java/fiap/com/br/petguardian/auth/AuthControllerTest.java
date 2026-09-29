package fiap.com.br.petguardian.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.com.br.petguardian.telefone.Telefone;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRole;
import fiap.com.br.petguardian.usuario.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve autenticar com sucesso e retornar token e perfil do usuario")
    void deveAutenticarComSucesso() throws Exception {
        var request = new AuthController.LoginRequest("enzo@fiap.com.br", "123456");

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Enzo")
                .email("enzo@fiap.com.br")
                .senha("hash")
                .role(UsuarioRole.PREMIUM)
                .telefone(Telefone.builder().ddd("11").numero("987654321").build())
                .enderecos(new HashSet<>())
                .build();

        var auth = new UsernamePasswordAuthenticationToken("enzo@fiap.com.br", "123456");

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenService.generateToken("enzo@fiap.com.br")).thenReturn("fake-jwt-token");
        when(usuarioService.findUsuarioByEmail("enzo@fiap.com.br")).thenReturn(usuario);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.nome").value("Enzo"))
                .andExpect(jsonPath("$.user.email").value("enzo@fiap.com.br"))
                .andExpect(jsonPath("$.user.role").value("PREMIUM"));
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized para credenciais inválidas")
    void deveRetornar401ParaCredenciaisInvalidas() throws Exception {
        var request = new AuthController.LoginRequest("enzo@fiap.com.br", "senhaErrada");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Credenciais invalidas."));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request para request inválido (email vazio)")
    void deveRetornar400ParaRequestInvalido() throws Exception {
        var request = new AuthController.LoginRequest("", "");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
