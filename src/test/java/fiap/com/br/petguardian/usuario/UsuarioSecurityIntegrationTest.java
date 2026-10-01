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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    @DisplayName("PUT /usuarios/{id} deve permitir dono atualizar seu cadastro com status 200")
    void devePermitirDonoAtualizarCadastro() throws Exception {
        var request = criarRequest("dono@fiap.com.br");
        Usuario usuarioSalvo = criarUsuarioMock(1L, "dono@fiap.com.br", UsuarioRole.COMUM);

        when(usuarioService.isOwner(1L, "dono@fiap.com.br")).thenReturn(true);
        when(usuarioService.update(eq(1L), any(UsuarioRequest.class))).thenReturn(usuarioSalvo);

        mockMvc.perform(put("/usuarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("dono@fiap.com.br").claim("role", "COMUM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("dono@fiap.com.br"));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} deve permitir dono alterar seu email com status 200")
    void devePermitirDonoAlterarEmail() throws Exception {
        var request = criarRequest("novo_email@fiap.com.br");
        Usuario usuarioSalvo = criarUsuarioMock(1L, "novo_email@fiap.com.br", UsuarioRole.COMUM);

        when(usuarioService.isOwner(1L, "antigo@fiap.com.br")).thenReturn(true);
        when(usuarioService.update(eq(1L), any(UsuarioRequest.class))).thenReturn(usuarioSalvo);

        mockMvc.perform(put("/usuarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("antigo@fiap.com.br").claim("role", "COMUM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("novo_email@fiap.com.br"));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} deve bloquear usuario COMUM tentando atualizar cadastro de outro usuario com 403")
    void deveBloquearUsuarioComumTentandoAtualizarOutroUsuario() throws Exception {
        var request = criarRequest("outro@fiap.com.br");

        when(usuarioService.isOwner(2L, "invasor@fiap.com.br")).thenReturn(false);

        mockMvc.perform(put("/usuarios/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("invasor@fiap.com.br").claim("role", "COMUM")))
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
    @DisplayName("DELETE /usuarios/{id} deve permitir dono deletar seu proprio cadastro com 204")
    void devePermitirDonoDeletarCadastro() throws Exception {
        when(usuarioService.isOwner(1L, "dono@fiap.com.br")).thenReturn(true);

        mockMvc.perform(delete("/usuarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("dono@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isNoContent());

        verify(usuarioService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} deve bloquear usuario COMUM tentando deletar outro usuario com 403")
    void deveBloquearUsuarioComumTentandoDeletarOutroUsuario() throws Exception {
        when(usuarioService.isOwner(2L, "invasor@fiap.com.br")).thenReturn(false);

        mockMvc.perform(delete("/usuarios/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("invasor@fiap.com.br").claim("role", "COMUM"))))
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
    @DisplayName("PATCH /usuarios/{id}/upgrade-premium deve permitir dono realizar upgrade para PREMIUM")
    void devePermitirDonoRealizarUpgradePremium() throws Exception {
        Usuario usuarioSalvo = criarUsuarioMock(1L, "dono@fiap.com.br", UsuarioRole.PREMIUM);
        when(usuarioService.isOwner(1L, "dono@fiap.com.br")).thenReturn(true);
        when(usuarioService.upgradePremium(1L)).thenReturn(usuarioSalvo);

        mockMvc.perform(patch("/usuarios/1/upgrade-premium")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("dono@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PREMIUM"));
    }

    @Test
    @DisplayName("PATCH /usuarios/{id}/upgrade-premium deve bloquear outro usuario tentando fazer upgrade com 403")
    void deveBloquearOutroUsuarioTentandoUpgradePremium() throws Exception {
        when(usuarioService.isOwner(2L, "invasor@fiap.com.br")).thenReturn(false);

        mockMvc.perform(patch("/usuarios/2/upgrade-premium")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("invasor@fiap.com.br").claim("role", "COMUM"))))
                .andExpect(status().isForbidden());
    }
}
