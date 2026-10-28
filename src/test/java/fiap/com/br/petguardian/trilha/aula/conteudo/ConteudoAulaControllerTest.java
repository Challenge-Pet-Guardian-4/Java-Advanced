package fiap.com.br.petguardian.trilha.aula.conteudo;

import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.com.br.petguardian.exception.GlobalExceptionHandler;
import fiap.com.br.petguardian.trilha.aula.conteudo.dto.ConteudoAulaRequest;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConteudoAulaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ConteudoAulaService conteudoService;

    @InjectMocks
    private ConteudoAulaController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private ConteudoAula criarConteudoMock(Long aulaId) {
        return ConteudoAula.builder()
                .id("mongo-1")
                .aulaId(aulaId)
                .tipoConteudo("ARTIGO")
                .corpoMarkdown("# Dica")
                .linksRecursos(List.of("http://video"))
                .atualizadoEm(Instant.now())
                .build();
    }

    @Test
    @DisplayName("GET /aulas/{aulaId}/conteudo deve buscar conteudo da aula")
    void deveBuscarConteudoPorAulaId() throws Exception {
        ConteudoAula conteudo = criarConteudoMock(100L);
        when(conteudoService.findByAulaId(100L)).thenReturn(conteudo);

        mockMvc.perform(get("/aulas/100/conteudo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aulaId").value(100))
                .andExpect(jsonPath("$.corpoMarkdown").value("# Dica"));
    }

    @Test
    @DisplayName("GET /aulas/conteudos deve listar todos os conteudos paginados")
    void deveListarConteudosPaginados() throws Exception {
        ConteudoAula conteudo = criarConteudoMock(100L);
        when(conteudoService.findAll(any())).thenReturn(new PageImpl<>(List.of(conteudo), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/aulas/conteudos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].aulaId").value(100));
    }

    @Test
    @DisplayName("POST /aulas/{aulaId}/conteudo deve criar conteudo")
    void deveCriarConteudo() throws Exception {
        var request = new ConteudoAulaRequest("ARTIGO", "# Markdown", List.of("http://video"));
        ConteudoAula conteudo = criarConteudoMock(100L);
        when(conteudoService.create(eq(100L), any())).thenReturn(conteudo);

        mockMvc.perform(post("/aulas/100/conteudo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aulaId").value(100));
    }

    @Test
    @DisplayName("PUT /aulas/{aulaId}/conteudo deve atualizar conteudo")
    void deveAtualizarConteudo() throws Exception {
        var request = new ConteudoAulaRequest("ARTIGO", "# Markdown Atualizado", List.of("http://video"));
        ConteudoAula conteudo = criarConteudoMock(100L);
        when(conteudoService.update(eq(100L), any())).thenReturn(conteudo);

        mockMvc.perform(put("/aulas/100/conteudo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aulaId").value(100));
    }

    @Test
    @DisplayName("DELETE /aulas/{aulaId}/conteudo deve deletar conteudo")
    void deveDeletarConteudo() throws Exception {
        mockMvc.perform(delete("/aulas/100/conteudo"))
                .andExpect(status().isNoContent());

        verify(conteudoService).deleteByAulaId(100L);
    }
}
