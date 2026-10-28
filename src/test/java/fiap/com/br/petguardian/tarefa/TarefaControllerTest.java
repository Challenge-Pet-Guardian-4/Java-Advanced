package fiap.com.br.petguardian.tarefa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fiap.com.br.petguardian.exception.GlobalExceptionHandler;
import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.tarefa.dto.TarefaRequest;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.tarefa.status.Status;
import fiap.com.br.petguardian.usuario.Usuario;
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
class TarefaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TarefaService tarefaService;

    @InjectMocks
    private TarefaController tarefaController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(tarefaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private Tarefa criarTarefaMock(Long id, String titulo) {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").build();
        Status status = Status.builder().id(1L).nomeStatus(EnumStatus.PENDENTE).build();
        return Tarefa.builder()
                .id(id)
                .titulo(titulo)
                .descricao("Desc")
                .prazo(LocalDateTime.now().plusDays(1))
                .pontosTarefa(10)
                .pet(pet)
                .usuario(usuario)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("GET /tarefas/me deve listar tarefas do cuidador")
    void deveListarTarefasDoCuidador() throws Exception {
        Tarefa tarefa = criarTarefaMock(50L, "Passeio");
        when(tarefaService.findAllByEmail(eq("enzo@fiap.com.br"), eq("ALL"), any()))
                .thenReturn(new PageImpl<>(List.of(tarefa), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        var auth = new UsernamePasswordAuthenticationToken("enzo@fiap.com.br", "123");

        mockMvc.perform(get("/tarefas/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Passeio"));
    }

    @Test
    @DisplayName("GET /tarefas/by-pet/{petId} deve listar tarefas por pet")
    void deveListarTarefasPorPet() throws Exception {
        Tarefa tarefa = criarTarefaMock(50L, "Passeio");
        when(tarefaService.findAllByPetId(eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(tarefa), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/tarefas/by-pet/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Passeio"));
    }

    @Test
    @DisplayName("GET /tarefas/{id} deve buscar tarefa por id")
    void deveBuscarTarefaPorId() throws Exception {
        Tarefa tarefa = criarTarefaMock(50L, "Passeio");
        when(tarefaService.findById(50L)).thenReturn(tarefa);

        mockMvc.perform(get("/tarefas/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Passeio"));
    }

    @Test
    @DisplayName("POST /tarefas deve criar tarefa")
    void deveCriarTarefa() throws Exception {
        var request = new TarefaRequest("Remédio", 10, "Dar remédio", LocalDateTime.now().plusHours(2), 10L, "PENDENTE", null);
        Tarefa tarefa = criarTarefaMock(51L, "Remédio");
        when(tarefaService.create(any(), eq("enzo@fiap.com.br"))).thenReturn(tarefa);

        var auth = new UsernamePasswordAuthenticationToken("enzo@fiap.com.br", "123");

        mockMvc.perform(post("/tarefas")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(51))
                .andExpect(jsonPath("$.titulo").value("Remédio"));
    }

    @Test
    @DisplayName("PUT /tarefas/{id} deve atualizar tarefa")
    void deveAtualizarTarefa() throws Exception {
        var request = new TarefaRequest("Remédio Atualizado", 10, "Dar remédio", LocalDateTime.now().plusHours(2), 10L, "PENDENTE", null);
        Tarefa tarefa = criarTarefaMock(50L, "Remédio Atualizado");
        when(tarefaService.update(eq(50L), any())).thenReturn(tarefa);

        mockMvc.perform(put("/tarefas/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Remédio Atualizado"));
    }

    @Test
    @DisplayName("PATCH /tarefas/{id}/concluir deve concluir tarefa")
    void deveConcluirTarefa() throws Exception {
        Tarefa tarefa = criarTarefaMock(50L, "Passeio");
        tarefa.setStatus(Status.builder().nomeStatus(EnumStatus.CONCLUIDO).build());
        when(tarefaService.concluir(50L)).thenReturn(tarefa);

        mockMvc.perform(patch("/tarefas/50/concluir"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));
    }

    @Test
    @DisplayName("PATCH /tarefas/{id}/desmarcar deve desmarcar tarefa")
    void deveDesmarcarTarefa() throws Exception {
        Tarefa tarefa = criarTarefaMock(50L, "Passeio");
        when(tarefaService.desmarcar(50L)).thenReturn(tarefa);

        mockMvc.perform(patch("/tarefas/50/desmarcar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    @DisplayName("DELETE /tarefas/{id} deve deletar tarefa")
    void deveDeletarTarefa() throws Exception {
        mockMvc.perform(delete("/tarefas/50"))
                .andExpect(status().isNoContent());

        verify(tarefaService).delete(50L);
    }

    @Test
    @DisplayName("GET /tarefas deve listar todas as tarefas paginadas")
    void deveListarTodasAsTarefas() throws Exception {
        Tarefa tarefa = criarTarefaMock(50L, "Passeio");
        when(tarefaService.findAll(any())).thenReturn(new PageImpl<>(List.of(tarefa), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Passeio"));
    }

    @Test
    @DisplayName("GET /tarefas/by-usuario/{usuarioId} deve listar tarefas por usuario")
    void deveListarTarefasPorUsuario() throws Exception {
        Tarefa tarefa = criarTarefaMock(50L, "Passeio");
        when(tarefaService.findAllByUsuario(eq(1L), eq("ALL"), any())).thenReturn(new PageImpl<>(List.of(tarefa), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/tarefas/by-usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Passeio"));
    }
}
