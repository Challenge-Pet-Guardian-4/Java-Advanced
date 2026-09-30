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
import org.springframework.http.HttpHeaders;
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

    private UsuarioRequest criarRequest(String role, String email) {
        return new UsuarioRequest(
                "Enzo",
                email,
                "senhaForte123",
                "11",
                "987654321",
                role,
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
        var request = criarRequest("COMUM", "dono@fiap.com.br");
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
        var request = criarRequest("COMUM", "novo_email@fiap.com.br");
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
        var request = criarRequest("COMUM", "outro@fiap.com.br");

        when(usuarioService.isOwner(2L, "invasor@fiap.com.br")).thenReturn(false);

        mockMvc.perform(put("/usuarios/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMUM")).jwt(jwt -> jwt.subject("invasor@fiap.com.br").claim("role", "COMUM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /usuarios/{id} deve permitir ADMIN atualizar cadastro de qualquer usuario inclusive com role ADMIN")
    void devePermitirAdminAtualizarQualquerCadastro() throws Exception {
        var request = criarRequest("ADMIN", "admin@fiap.com.br");
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
        var request = criarRequest("COMUM", "qualquer@fiap.com.br");

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
    @DisplayName("POST /usuarios tentando cadastrar como ADMIN deve retornar 400 Bad Request")
    void deveRejeitarCadastroPublicoComoAdmin() throws Exception {
        var request = criarRequest("ADMIN", "hacker@fiap.com.br");

        when(usuarioService.create(any(UsuarioRequest.class)))
                .thenThrow(new IllegalArgumentException("Cadastro público não permite perfil de administrador."));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cadastro público não permite perfil de administrador."));
    }
}
