package fiap.com.br.petguardian.trilha.aula;

import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.com.br.petguardian.exception.GlobalExceptionHandler;
import fiap.com.br.petguardian.trilha.aula.dto.AulaRequest;
import fiap.com.br.petguardian.trilha.aula.dto.AulaResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AulaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AulaService aulaService;

    @InjectMocks
    private AulaController aulaController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(aulaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private AulaResponse criarAulaResponseMock(Long id, String nome) {
        return new AulaResponse(id, nome, "Desc", 10, DificuldadeAula.FACIL, "# Conteudo", false, 10L, "Módulo 1");
    }

    @Test
    @DisplayName("GET /aulas/{id} deve buscar aula por id")
    void deveBuscarAulaPorId() throws Exception {
        AulaResponse response = criarAulaResponseMock(100L, "Senta");
        when(aulaService.findById(100L)).thenReturn(response);

        mockMvc.perform(get("/aulas/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Senta"));
    }

    @Test
    @DisplayName("GET /aulas/modulo/{moduloId} deve listar aulas por modulo")
    void deveListarAulasPorModulo() throws Exception {
        AulaResponse response = criarAulaResponseMock(100L, "Senta");
        when(aulaService.findAllByModuloId(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/aulas/modulo/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Senta"));
    }

    @Test
    @DisplayName("GET /aulas deve listar todas as aulas paginadas")
    void deveListarTodasAulas() throws Exception {
        AulaResponse response = criarAulaResponseMock(100L, "Senta");
        when(aulaService.findAll(any())).thenReturn(new PageImpl<>(List.of(response), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/aulas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Senta"));
    }

    @Test
    @DisplayName("PATCH /aulas/{id}/concluir deve concluir aula")
    void deveConcluirAula() throws Exception {
        AulaResponse response = new AulaResponse(100L, "Senta", "Desc", 10, DificuldadeAula.FACIL, "# Conteudo", true, 10L, "Módulo 1");
        when(aulaService.concluir(eq(100L), eq("enzo@fiap.com.br"))).thenReturn(response);

        var auth = new UsernamePasswordAuthenticationToken("enzo@fiap.com.br", "123");

        mockMvc.perform(patch("/aulas/100/concluir").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluida").value(true));
    }

    @Test
    @DisplayName("PATCH /aulas/{id}/desmarcar deve desmarcar aula")
    void deveDesmarcarAula() throws Exception {
        AulaResponse response = criarAulaResponseMock(100L, "Senta");
        when(aulaService.desmarcar(eq(100L), eq("enzo@fiap.com.br"))).thenReturn(response);

        var auth = new UsernamePasswordAuthenticationToken("enzo@fiap.com.br", "123");

        mockMvc.perform(patch("/aulas/100/desmarcar").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluida").value(false));
    }

    @Test
    @DisplayName("POST /aulas deve criar aula")
    void deveCriarAula() throws Exception {
        var request = new AulaRequest("Senta", "Desc", 10, "FACIL", false, 10L);
        AulaResponse response = criarAulaResponseMock(100L, "Senta");
        when(aulaService.create(any())).thenReturn(response);

        mockMvc.perform(post("/aulas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("PUT /aulas/{id} deve atualizar aula")
    void deveAtualizarAula() throws Exception {
        var request = new AulaRequest("Senta Atualizado", "Desc", 10, "MEDIO", false, 10L);
        AulaResponse response = criarAulaResponseMock(100L, "Senta Atualizado");
        when(aulaService.update(eq(100L), any())).thenReturn(response);

        mockMvc.perform(put("/aulas/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Senta Atualizado"));
    }

    @Test
    @DisplayName("DELETE /aulas/{id} deve deletar aula")
    void deveDeletarAula() throws Exception {
        mockMvc.perform(delete("/aulas/100"))
                .andExpect(status().isNoContent());

        verify(aulaService).delete(100L);
    }
}
