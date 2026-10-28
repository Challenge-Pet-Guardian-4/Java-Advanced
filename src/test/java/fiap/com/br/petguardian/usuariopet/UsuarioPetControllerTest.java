package fiap.com.br.petguardian.usuariopet;

import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.com.br.petguardian.exception.GlobalExceptionHandler;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorRequest;
import fiap.com.br.petguardian.usuariopet.dto.CoCuidadorResponse;
import fiap.com.br.petguardian.usuariopet.dto.TransferirResponsabilidadeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioPetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioPetService usuarioPetService;

    @InjectMocks
    private UsuarioPetController usuarioPetController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioPetController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("DELETE /pets/{petId}/cuidadores/me deve sair do care circle")
    void deveSairDoCareCircle() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken("enzo@fiap.com.br", "123");

        mockMvc.perform(delete("/pets/10/cuidadores/me").principal(auth))
                .andExpect(status().isNoContent());

        verify(usuarioPetService).desvincularCuidador(10L, "enzo@fiap.com.br");
    }

    @Test
    @DisplayName("GET /pets/{petId}/cuidadores deve listar cuidadores")
    void deveListarCuidadores() throws Exception {
        var cuidador = new CoCuidadorResponse("Enzo", "enzo@fiap.com.br", 10L, "Thor", true);
        when(usuarioPetService.listarCuidadoresDoPet(10L)).thenReturn(List.of(cuidador));

        mockMvc.perform(get("/pets/10/cuidadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Enzo"));
    }

    @Test
    @DisplayName("POST /pets/{petId}/cuidadores deve convidar cuidador")
    void deveConvidarCuidador() throws Exception {
        var request = new CoCuidadorRequest("luna@fiap.com.br");
        var response = new CoCuidadorResponse("Luna", "luna@fiap.com.br", 10L, "Thor", false);
        when(usuarioPetService.convidarCoCuidador(eq(10L), any())).thenReturn(response);

        mockMvc.perform(post("/pets/10/cuidadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Luna"));
    }

    @Test
    @DisplayName("DELETE /pets/{petId}/cuidadores deve desvincular cuidador por email")
    void deveDesvincularCuidador() throws Exception {
        mockMvc.perform(delete("/pets/10/cuidadores").param("email", "luna@fiap.com.br"))
                .andExpect(status().isNoContent());

        verify(usuarioPetService).desvincularCuidador(10L, "luna@fiap.com.br");
    }

    @Test
    @DisplayName("PATCH /pets/{petId}/responsavel-principal deve transferir responsabilidade")
    void deveTransferirResponsabilidade() throws Exception {
        var request = new TransferirResponsabilidadeRequest("luna@fiap.com.br");

        mockMvc.perform(patch("/pets/10/responsavel-principal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(usuarioPetService).transferirResponsabilidadePrincipal(eq(10L), any());
    }
}
