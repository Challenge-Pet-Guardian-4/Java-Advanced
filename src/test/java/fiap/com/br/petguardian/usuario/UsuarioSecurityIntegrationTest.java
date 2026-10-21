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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    @DisplayName("PATCH /usuarios/me/upgrade-premium deve permitir usuario autenticado realizar upgrade com 200")
    void devePermitirUsuarioAutenticadoUpgradePremium() throws Exception {
        Usuario usuarioSalvo = criarUsuarioMock(1L, "me@fiap.com.br", UsuarioRole.PREMIUM);
        when(usuarioService.upgradePremium("me@fiap.com.br")).thenReturn(usuarioSalvo);

        mockMvc.perform(patch("/usuarios/me/upgrade-premium")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("me@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PREMIUM"));
    }
}
