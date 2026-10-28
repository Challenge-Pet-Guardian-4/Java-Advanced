package fiap.com.br.petguardian.trilha;

import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.com.br.petguardian.exception.GlobalExceptionHandler;
import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.trilha.dto.TrilhaCompletaResponse;
import fiap.com.br.petguardian.trilha.dto.TrilhaRequest;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrilhaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TrilhaService trilhaService;

    @InjectMocks
    private TrilhaController trilhaController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(trilhaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private Trilha criarTrilhaMock(Long id, String nome) {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        return Trilha.builder()
                .id(id)
                .nome(nome)
                .descricao("Desc")
                .pet(pet)
                .modulos(Set.of())
                .build();
    }

    @Test
    @DisplayName("GET /trilhas/me deve listar trilhas completas do usuario")
    void deveListarTrilhasCompletas() throws Exception {
        var trilha = new TrilhaCompletaResponse(1L, "Obediência", "Desc", 10L, "Thor", List.of());
        when(trilhaService.findTrilhasCompletasByUsuarioEmail("enzo@fiap.com.br")).thenReturn(List.of(trilha));

        var auth = new UsernamePasswordAuthenticationToken("enzo@fiap.com.br", "123");

        mockMvc.perform(get("/trilhas/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Obediência"));
    }

    @Test
    @DisplayName("GET /trilhas/{id} deve buscar trilha por id")
    void deveBuscarTrilhaPorId() throws Exception {
        Trilha trilha = criarTrilhaMock(1L, "Obediência");
        when(trilhaService.findById(1L)).thenReturn(trilha);

        mockMvc.perform(get("/trilhas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Obediência"));
    }

    @Test
    @DisplayName("GET /trilhas deve listar todas as trilhas")
    void deveListarTodasTrilhas() throws Exception {
        Trilha trilha = criarTrilhaMock(1L, "Obediência");
        when(trilhaService.findAll(any())).thenReturn(new PageImpl<>(List.of(trilha), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/trilhas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Obediência"));
    }

    

    @Test
    @DisplayName("POST /trilhas deve criar trilha")
    void deveCriarTrilha() throws Exception {
        var request = new TrilhaRequest("Obediência", "Desc", 10L);
        Trilha trilha = criarTrilhaMock(1L, "Obediência");
        when(trilhaService.create(any())).thenReturn(trilha);

        mockMvc.perform(post("/trilhas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /trilhas/{id} deve atualizar trilha")
    void deveAtualizarTrilha() throws Exception {
        var request = new TrilhaRequest("Obediência Atualizada", "Desc", 10L);
        Trilha trilha = criarTrilhaMock(1L, "Obediência Atualizada");
        when(trilhaService.update(eq(1L), any())).thenReturn(trilha);

        mockMvc.perform(put("/trilhas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Obediência Atualizada"));
    }

    @Test
    @DisplayName("DELETE /trilhas/{id} deve deletar trilha")
    void deveDeletarTrilha() throws Exception {
        mockMvc.perform(delete("/trilhas/1"))
                .andExpect(status().isNoContent());

        verify(trilhaService).delete(1L);
    }
}
