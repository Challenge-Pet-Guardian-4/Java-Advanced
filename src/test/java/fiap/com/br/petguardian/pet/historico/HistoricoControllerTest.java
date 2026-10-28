package fiap.com.br.petguardian.pet.historico;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fiap.com.br.petguardian.exception.GlobalExceptionHandler;
import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.historico.dto.HistoricoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HistoricoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HistoricoService historicoService;

    @InjectMocks
    private HistoricoController historicoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(historicoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private Historico criarHistoricoMock(Long id, String tipo) {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        return Historico.builder()
                .id(id)
                .tipoHist(tipo)
                .dataHist(LocalDateTime.now())
                .pet(pet)
                .build();
    }

    @Test
    @DisplayName("GET /historicos/me deve listar historico do usuario autenticado")
    void deveListarHistoricoDoUsuario() throws Exception {
        Historico hist = criarHistoricoMock(1L, "Vacina");
        when(historicoService.findAllByUsuarioEmail("enzo@fiap.com.br")).thenReturn(List.of(hist));

        var auth = new UsernamePasswordAuthenticationToken("enzo@fiap.com.br", "123");

        mockMvc.perform(get("/historicos/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoHist").value("Vacina"));
    }

    @Test
    @DisplayName("GET /historicos/pet/{petId} deve listar historico por pet")
    void deveListarHistoricoPorPet() throws Exception {
        Historico hist = criarHistoricoMock(1L, "Vacina");
        when(historicoService.findAllByPetId(10L)).thenReturn(List.of(hist));

        mockMvc.perform(get("/historicos/pet/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoHist").value("Vacina"));
    }

    @Test
    @DisplayName("GET /historicos/{id} deve buscar historico por id")
    void deveBuscarHistoricoPorId() throws Exception {
        Historico hist = criarHistoricoMock(1L, "Vacina");
        when(historicoService.findById(1L)).thenReturn(hist);

        mockMvc.perform(get("/historicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoHist").value("Vacina"));
    }

    @Test
    @DisplayName("GET /historicos deve listar todos os registros paginados")
    void deveListarTodosHistoricosPaginados() throws Exception {
        Historico hist = criarHistoricoMock(1L, "Vacina");
        when(historicoService.findAll(any())).thenReturn(new PageImpl<>(List.of(hist), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/historicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tipoHist").value("Vacina"));
    }

    @Test
    @DisplayName("POST /historicos deve criar historico")
    void deveCriarHistorico() throws Exception {
        var request = new HistoricoRequest("Consulta", LocalDateTime.now(), 10L);
        Historico hist = criarHistoricoMock(1L, "Consulta");
        when(historicoService.create(any())).thenReturn(hist);

        mockMvc.perform(post("/historicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /historicos/{id} deve atualizar historico")
    void deveAtualizarHistorico() throws Exception {
        var request = new HistoricoRequest("Consulta Atualizada", LocalDateTime.now(), 10L);
        Historico hist = criarHistoricoMock(1L, "Consulta Atualizada");
        when(historicoService.update(eq(1L), any())).thenReturn(hist);

        mockMvc.perform(put("/historicos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoHist").value("Consulta Atualizada"));
    }

    @Test
    @DisplayName("DELETE /historicos/{id} deve deletar historico")
    void deveDeletarHistorico() throws Exception {
        mockMvc.perform(delete("/historicos/1"))
                .andExpect(status().isNoContent());

        verify(historicoService).delete(1L);
    }
}
