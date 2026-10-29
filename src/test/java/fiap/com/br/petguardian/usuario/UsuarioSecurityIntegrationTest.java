package fiap.com.br.petguardian.usuario;

import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.com.br.petguardian.auth.TokenService;
import fiap.com.br.petguardian.endereco.Endereco;
import fiap.com.br.petguardian.endereco.bairro.Bairro;
import fiap.com.br.petguardian.endereco.cidade.Cidade;
import fiap.com.br.petguardian.endereco.dto.EnderecoRequest;
import fiap.com.br.petguardian.endereco.estado.Estado;
import fiap.com.br.petguardian.telefone.Telefone;
import fiap.com.br.petguardian.usuario.dto.UsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.core.TypeInformation;

import java.util.List;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private fiap.com.br.petguardian.usuariopet.UsuarioPetService usuarioPetService;

    private UsuarioRequest criarRequest(String email) {
        return new UsuarioRequest(
                "Enzo",
                email,
                "senhaForte123",
                "11",
                "987654321",
                new EnderecoRequest("01310100", "100")
        );
    }

    private Usuario criarUsuarioMock(Long id, String email, UsuarioRole role) {
        Estado estado = Estado.builder().id(1L).nome("SP").build();
        Cidade cidade = Cidade.builder().id(1L).nome("São Paulo").estado(estado).build();
        Bairro bairro = Bairro.builder().id(1L).nome("Bela Vista").cidade(cidade).build();

        return Usuario.builder()
                .id(id)
                .nome("Enzo")
                .email(email)
                .senha("hash")
                .role(role)
                .telefone(Telefone.builder().id(1L).ddd("11").numero("987654321").build())
                .endereco(Endereco.builder().id(1L).cep("01310100").numero("100").rua("Paulista").bairro(bairro).build())
                .build();
    }

    @Test
    @DisplayName("GET /usuarios/{id} deve bloquear usuario COMUM com 403")
    void deveBloquearUsuarioComumNoGetPorId() throws Exception {
        mockMvc.perform(get("/usuarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("comum@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /usuarios/{id} deve permitir ADMIN buscar usuario por ID com 200")
    void devePermitirAdminBuscarUsuarioPorId() throws Exception {
        Usuario usuario = criarUsuarioMock(1L, "user@fiap.com.br", UsuarioRole.COMUM);
        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(get("/usuarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@fiap.com.br"));
    }

    @Test
    @DisplayName("GET /usuarios/{id} sem autenticacao deve retornar 401")
    void deveRetornar401NoGetSemAutenticacao() throws Exception {
        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /usuarios/{id} deve bloquear usuario COMUM com 403")
    void deveBloquearUsuarioComumTentandoAtualizarPorId() throws Exception {
        var request = criarRequest("outro@fiap.com.br");

        mockMvc.perform(put("/usuarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("comum@fiap.com.br").claim("role", "COMUM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /usuarios/{id} deve permitir ADMIN atualizar cadastro de qualquer usuario")
    void devePermitirAdminAtualizarQualquerCadastro() throws Exception {
        var request = criarRequest("admin@fiap.com.br");
        Usuario usuarioSalvo = criarUsuarioMock(2L, "admin@fiap.com.br", UsuarioRole.ADMIN);

        when(usuarioService.update(eq(2L), any(UsuarioRequest.class))).thenReturn(usuarioSalvo);

        mockMvc.perform(put("/usuarios/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} sem autenticacao deve retornar 401")
    void deveRetornar401NoPutSemAutenticacao() throws Exception {
        var request = criarRequest("qualquer@fiap.com.br");

        mockMvc.perform(put("/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} deve bloquear usuario COMUM com 403")
    void deveBloquearUsuarioComumTentandoDeletarPorId() throws Exception {
        mockMvc.perform(delete("/usuarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("comum@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} deve permitir ADMIN deletar qualquer usuario com 204")
    void devePermitirAdminDeletarQualquerUsuario() throws Exception {
        mockMvc.perform(delete("/usuarios/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isNoContent());

        verify(usuarioService).delete(2L);
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} sem autenticacao deve retornar 401")
    void deveRetornar401NoDeleteSemAutenticacao() throws Exception {
        mockMvc.perform(delete("/usuarios/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /usuarios cadastro publico deve criar usuario com sucesso e status 201")
    void deveCriarUsuarioComSucesso() throws Exception {
        var request = criarRequest("novo@fiap.com.br");
        Usuario usuarioSalvo = criarUsuarioMock(1L, "novo@fiap.com.br", UsuarioRole.COMUM);

        when(usuarioService.create(any(UsuarioRequest.class))).thenReturn(usuarioSalvo);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").value("COMUM"));
    }

    @Test
    @DisplayName("PATCH /usuarios/{id}/role deve permitir ADMIN alterar a role do usuario")
    void devePermitirAdminAlterarRole() throws Exception {
        Usuario usuarioSalvo = criarUsuarioMock(1L, "user@fiap.com.br", UsuarioRole.ADMIN);
        when(usuarioService.updateRole(1L, "ADMIN")).thenReturn(usuarioSalvo);

        mockMvc.perform(patch("/usuarios/1/role")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("PATCH /usuarios/{id}/role deve bloquear usuario COMUM tentando alterar role com 403")
    void deveBloquearUsuarioComumTentandoAlterarRole() throws Exception {
        mockMvc.perform(patch("/usuarios/1/role")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("user@fiap.com.br").claim("role", "COMUM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /usuarios/{id}/upgrade-premium deve bloquear usuario COMUM com 403")
    void deveBloquearUsuarioComumNoUpgradePremiumPorId() throws Exception {
        mockMvc.perform(patch("/usuarios/1/upgrade-premium")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("comum@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /usuarios/{id}/upgrade-premium deve permitir ADMIN realizar upgrade para PREMIUM com 200")
    void devePermitirAdminRealizarUpgradePremium() throws Exception {
        Usuario usuarioSalvo = criarUsuarioMock(1L, "dono@fiap.com.br", UsuarioRole.PREMIUM);
        when(usuarioService.findById(1L)).thenReturn(usuarioSalvo);
        when(usuarioService.upgradePremium("dono@fiap.com.br")).thenReturn(usuarioSalvo);

        mockMvc.perform(patch("/usuarios/1/upgrade-premium")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PREMIUM"));
    }

    @Test
    @DisplayName("GET /usuarios/me deve permitir usuario autenticado buscar seu perfil com 200")
    void devePermitirBuscarProprioPerfil() throws Exception {
        Usuario usuario = criarUsuarioMock(1L, "me@fiap.com.br", UsuarioRole.COMUM);
        when(usuarioService.findByEmail("me@fiap.com.br")).thenReturn(usuario);

        mockMvc.perform(get("/usuarios/me")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("me@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@fiap.com.br"));
    }

    @Test
    @DisplayName("PUT /usuarios/me deve permitir usuario autenticado atualizar seu perfil e retornar header X-Novo-Token")
    void devePermitirAtualizarProprioPerfilComHeaderXNovoToken() throws Exception {
        var request = criarRequest("novo_email@fiap.com.br");
        Usuario usuarioSalvo = criarUsuarioMock(1L, "novo_email@fiap.com.br", UsuarioRole.COMUM);

        when(usuarioService.update(eq("me@fiap.com.br"), any(UsuarioRequest.class))).thenReturn(usuarioSalvo);
        when(tokenService.generateToken(any())).thenReturn("novo-token");

        mockMvc.perform(put("/usuarios/me")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("me@fiap.com.br").claim("role", "COMUM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Novo-Token", "novo-token"))
                .andExpect(jsonPath("$.email").value("novo_email@fiap.com.br"));
    }

    @Test
    @DisplayName("DELETE /usuarios/me deve permitir usuario autenticado deletar seu perfil com 204")
    void devePermitirDeletarProprioPerfil() throws Exception {
        mockMvc.perform(delete("/usuarios/me")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("me@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isNoContent());

        verify(usuarioService).delete("me@fiap.com.br");
    }

    @Test
    @DisplayName("DELETE /usuarios/me deve impedir ADMIN de se autoexcluir retornando 400")
    void deveImpedirAdminDeSeAutoexcluir() throws Exception {
        doThrow(new IllegalArgumentException("Administrador não pode se autoexcluir para evitar perda de acesso ao sistema."))
                .when(usuarioService).delete("admin@fiap.com.br");

        mockMvc.perform(delete("/usuarios/me")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Administrador não pode se autoexcluir para evitar perda de acesso ao sistema."));
    }

    @Test
    @DisplayName("PATCH /usuarios/me/upgrade-premium deve permitir usuario autenticado realizar upgrade com 200 e X-Novo-Token")
    void devePermitirUsuarioAutenticadoUpgradePremium() throws Exception {
        Usuario usuarioSalvo = criarUsuarioMock(1L, "me@fiap.com.br", UsuarioRole.PREMIUM);
        when(usuarioService.upgradePremium("me@fiap.com.br")).thenReturn(usuarioSalvo);
        when(tokenService.generateToken("me@fiap.com.br")).thenReturn("novo-token-premium");

        mockMvc.perform(patch("/usuarios/me/upgrade-premium")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("me@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Novo-Token", "novo-token-premium"))
                .andExpect(jsonPath("$.role").value("PREMIUM"));
    }

    @Test
    @DisplayName("GET /actuator/metrics deve bloquear usuario COMUM com 403")
    void deveBloquearUsuarioComumEmActuatorMetrics() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("comum@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /actuator/metrics deve permitir usuario ADMIN com 200")
    void devePermitirAdminEmActuatorMetrics() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /actuator/caches deve bloquear usuario COMUM com 403")
    void deveBloquearUsuarioComumEmActuatorCaches() throws Exception {
        mockMvc.perform(get("/actuator/caches")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("comum@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /actuator/caches deve permitir usuario ADMIN com 200")
    void devePermitirAdminEmActuatorCaches() throws Exception {
        mockMvc.perform(get("/actuator/caches")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /actuator/health deve permitir acesso publico anonimo sem 401 ou 403 e sem detalhes")
    void devePermitirAcessoAnonimoEmActuatorHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(status == 200 || status == 503,
                            "Esperava 200 (UP) ou 503 (DOWN), mas recebeu " + status);
                })
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    @DisplayName("GET /usuarios/abc com ADMIN deve retornar 400 por MethodArgumentTypeMismatchException")
    void deveRetornar400AoPassarParametroComTipoInvalido() throws Exception {
        mockMvc.perform(get("/usuarios/abc")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("GET /usuarios com ordenacao invalida deve retornar 400 por PropertyReferenceException")
    void deveRetornar400AoPassarPropriedadeDeOrdenacaoInvalida() throws Exception {
        when(usuarioService.findAll(any(Pageable.class)))
                .thenThrow(new PropertyReferenceException("campoInvalido", TypeInformation.of(Usuario.class), List.of()));

        mockMvc.perform(get("/usuarios?sort=campoInvalido,desc")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Propriedade de ordenação 'campoInvalido' é inválida para a entidade."));
    }

    @Test
    @DisplayName("PATCH /usuarios deve retornar 405 Method Not Allowed")
    void deveRetornar405AoChamarMetodoNaoSuportado() throws Exception {
        mockMvc.perform(patch("/usuarios")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("POST /usuarios com Content-Type invalido deve retornar 415 Unsupported Media Type")
    void deveRetornar415AoEnviarContentTypeNaoSuportado() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("texto qualquer"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
    @Test
    @DisplayName("GET /admin deve permitir acesso publico anonimo e redirecionar para /admin/index.html")
    void devePermitirAcessoPublicoEmAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /usuarios/me/rede-cuidado deve retornar rede de cuidado do usuario logado")
    void deveRetornarRedeCuidadoUsuarioLogado() throws Exception {
        Usuario usuario = criarUsuarioMock(1L, "comum@fiap.com.br", UsuarioRole.COMUM);
        var rede = fiap.com.br.petguardian.usuariopet.dto.RedeCuidadoResponse.empty(usuario);
        when(usuarioPetService.montarRedeCuidado("comum@fiap.com.br")).thenReturn(rede);

        mockMvc.perform(get("/usuarios/me/rede-cuidado")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("comum@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailUsuario").value("comum@fiap.com.br"));
    }

    @Test
    @DisplayName("GET /usuarios deve permitir ADMIN listar todos os usuarios paginados")
    void devePermitirAdminListarTodosOsUsuarios() throws Exception {
        Usuario usuario = criarUsuarioMock(1L, "user@fiap.com.br", UsuarioRole.COMUM);
        when(usuarioService.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(usuario), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/usuarios")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("user@fiap.com.br"));
    }

    @Test
    @DisplayName("GET /usuarios/by-email deve permitir ADMIN buscar usuario por email")
    void devePermitirAdminBuscarPorEmail() throws Exception {
        Usuario usuario = criarUsuarioMock(1L, "user@fiap.com.br", UsuarioRole.COMUM);
        when(usuarioService.findByEmail("user@fiap.com.br")).thenReturn(usuario);

        mockMvc.perform(get("/usuarios/by-email?email=user@fiap.com.br")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@fiap.com.br"));
    }

    @Test
    @DisplayName("GET /usuarios/{id}/rede-cuidado deve permitir ADMIN visualizar rede de cuidado")
    void devePermitirAdminVerRedeCuidadoPorId() throws Exception {
        Usuario usuario = criarUsuarioMock(1L, "user@fiap.com.br", UsuarioRole.COMUM);
        var rede = fiap.com.br.petguardian.usuariopet.dto.RedeCuidadoResponse.empty(usuario);
        when(usuarioPetService.montarRedeCuidado(1L)).thenReturn(rede);

        mockMvc.perform(get("/usuarios/1/rede-cuidado")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailUsuario").value("user@fiap.com.br"));
    }

    @Test
    @DisplayName("PATCH /usuarios/{id}/upgrade-premium deve permitir ADMIN atualizar usuario para PREMIUM")
    void devePermitirAdminUpgradePremiumPorId() throws Exception {
        Usuario usuario = criarUsuarioMock(1L, "user@fiap.com.br", UsuarioRole.COMUM);
        Usuario usuarioPremium = criarUsuarioMock(1L, "user@fiap.com.br", UsuarioRole.PREMIUM);

        when(usuarioService.findById(1L)).thenReturn(usuario);
        when(usuarioService.upgradePremium("user@fiap.com.br")).thenReturn(usuarioPremium);

        mockMvc.perform(patch("/usuarios/1/upgrade-premium")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).jwt(jwt -> jwt.subject("admin@fiap.com.br").claim("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PREMIUM"));
    }
}
