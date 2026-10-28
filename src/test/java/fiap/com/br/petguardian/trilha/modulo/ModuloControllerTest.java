package fiap.com.br.petguardian.trilha.modulo;

import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.com.br.petguardian.exception.GlobalExceptionHandler;
import fiap.com.br.petguardian.trilha.Trilha;
import fiap.com.br.petguardian.trilha.modulo.dto.ModuloRequest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ModuloControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ModuloService moduloService;

    @InjectMocks
    private ModuloController moduloController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(moduloController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private Modulo criarModuloMock(Long id, String nome) {
        Trilha trilha = Trilha.builder().id(1L).nome("Obediência").build();
        return Modulo.builder()
                .id(id)
                .nome(nome)
                .tempoConclusao("5")
                .descricao("Desc")
                .trilha(trilha)
                .aulas(Set.of())
                .build();
    }

    @Test
    @DisplayName("GET /modulos/{id} deve buscar modulo por id")
    void deveBuscarModuloPorId() throws Exception {
        Modulo modulo = criarModuloMock(10L, "Módulo 1");
        when(moduloService.findById(10L)).thenReturn(modulo);

        mockMvc.perform(get("/modulos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Módulo 1"));
    }

    @Test
    @DisplayName("GET /modulos/trilha/{trilhaId} deve listar modulos por trilha")
    void deveListarModulosPorTrilha() throws Exception {
        Modulo modulo = criarModuloMock(10L, "Módulo 1");
        when(moduloService.findAllByTrilhaId(1L)).thenReturn(List.of(modulo));

        mockMvc.perform(get("/modulos/trilha/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Módulo 1"));
    }

    @Test
    @DisplayName("GET /modulos deve listar todos os modulos paginados")
    void deveListarTodosModulos() throws Exception {
        Modulo modulo = criarModuloMock(10L, "Módulo 1");
        when(moduloService.findAll(any())).thenReturn(new PageImpl<>(List.of(modulo), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/modulos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Módulo 1"));
    }

    @Test
    @DisplayName("POST /modulos deve criar modulo")
    void deveCriarModulo() throws Exception {
        var request = new ModuloRequest("Módulo 1", "5", "Desc", 1L);
        Modulo modulo = criarModuloMock(10L, "Módulo 1");
        when(moduloService.create(any())).thenReturn(modulo);

        mockMvc.perform(post("/modulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("PUT /modulos/{id} deve atualizar modulo")
    void deveAtualizarModulo() throws Exception {
        var request = new ModuloRequest("Módulo Atualizado", "6", "Desc", 1L);
        Modulo modulo = criarModuloMock(10L, "Módulo Atualizado");
        when(moduloService.update(eq(10L), any())).thenReturn(modulo);

        mockMvc.perform(put("/modulos/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Módulo Atualizado"));
    }

    @Test
    @DisplayName("DELETE /modulos/{id} deve deletar modulo")
    void deveDeletarModulo() throws Exception {
        mockMvc.perform(delete("/modulos/10"))
                .andExpect(status().isNoContent());

        verify(moduloService).delete(10L);
    }
}
