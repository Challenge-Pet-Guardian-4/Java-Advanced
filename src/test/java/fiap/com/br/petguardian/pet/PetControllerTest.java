package fiap.com.br.petguardian.pet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fiap.com.br.petguardian.exception.GlobalExceptionHandler;
import fiap.com.br.petguardian.pet.dto.PetDetailResponse;
import fiap.com.br.petguardian.pet.dto.PetPontuacaoAgregadaResponse;
import fiap.com.br.petguardian.pet.dto.PetPontuacaoResponse;
import fiap.com.br.petguardian.pet.dto.PetRequest;
import fiap.com.br.petguardian.pet.dto.PetResponse;
import fiap.com.br.petguardian.pet.raca.Raca;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PetService petService;

    @InjectMocks
    private PetController petController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(petController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private Pet criarPetMock(Long id, String nome) {
        Raca raca = Raca.builder().id(1L).nome("Golden Retriever").build();
        return Pet.builder()
                .id(id)
                .nome(nome)
                .raca(raca)
                .porte(PetPorte.GRANDE)
                .sexo('M')
                .castrado(true)
                .dataNasc(LocalDate.now().minusYears(2))
                .build();
    }

    @Test
    @DisplayName("GET /pets/me deve listar pets do usuario autenticado")
    void deveListarPetsDoUsuario() throws Exception {
        Pet pet = criarPetMock(10L, "Thor");
        when(petService.findByEmail(eq("enzo@fiap.com.br"), any())).thenReturn(new PageImpl<>(List.of(pet), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        var auth = new UsernamePasswordAuthenticationToken("enzo@fiap.com.br", "123");

        mockMvc.perform(get("/pets/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Thor"));
    }

    @Test
    @DisplayName("GET /pets/me/pontos deve retornar pontuacao agregada dos pets")
    void deveRetornarPontuacaoAgregada() throws Exception {
        var pontuacao = new PetPontuacaoAgregadaResponse(100, 50, 150, "[]");
        when(petService.calcularPontuacaoAgregadaPetsUsuario("enzo@fiap.com.br")).thenReturn(pontuacao);

        var auth = new UsernamePasswordAuthenticationToken("enzo@fiap.com.br", "123");

        mockMvc.perform(get("/pets/me/pontos").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pontosTotais").value(150));
    }

    @Test
    @DisplayName("POST /pets deve criar pet associado ao usuario autenticado")
    void deveCriarPet() throws Exception {
        var request = new PetRequest("Thor", LocalDate.now().minusYears(2), "Golden Retriever", "GRANDE", 'M', true);
        Pet pet = criarPetMock(10L, "Thor");

        when(petService.create(any(), eq("enzo@fiap.com.br"))).thenReturn(pet);

        var auth = new UsernamePasswordAuthenticationToken("enzo@fiap.com.br", "123");

        mockMvc.perform(post("/pets")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Thor"));
    }

    @Test
    @DisplayName("GET /pets/{id} deve buscar pet por id")
    void deveBuscarPetPorId() throws Exception {
        Pet pet = criarPetMock(10L, "Thor");
        when(petService.findById(10L)).thenReturn(pet);

        mockMvc.perform(get("/pets/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Thor"));
    }

    @Test
    @DisplayName("GET /pets/{id}/pontos deve consultar pontuacao do pet")
    void deveConsultarPontosPet() throws Exception {
        var pontos = new PetPontuacaoResponse(10L, "Thor", 100, 50, 150);
        when(petService.calcularPontuacaoTotalPet(10L)).thenReturn(pontos);

        mockMvc.perform(get("/pets/10/pontos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pontosTotais").value(150));
    }

    @Test
    @DisplayName("GET /pets/{id}/detalhe deve buscar ficha consolidada")
    void deveBuscarFichaConsolidada() throws Exception {
        var petResp = PetResponse.fromEntity(criarPetMock(10L, "Thor"));
        var pontosResp = new PetPontuacaoResponse(10L, "Thor", 100, 50, 150);
        var detalhe = new PetDetailResponse(petResp, pontosResp, List.of(), List.of(), List.of());
        when(petService.getPetDetail(10L)).thenReturn(detalhe);

        mockMvc.perform(get("/pets/10/detalhe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pet.nome").value("Thor"));
    }

    @Test
    @DisplayName("PUT /pets/{id} deve atualizar pet")
    void deveAtualizarPet() throws Exception {
        var request = new PetRequest("Thor Atualizado", LocalDate.now().minusYears(2), "Golden Retriever", "GRANDE", 'M', true);
        Pet pet = criarPetMock(10L, "Thor Atualizado");

        when(petService.update(eq(10L), any())).thenReturn(pet);

        mockMvc.perform(put("/pets/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Thor Atualizado"));
    }

    @Test
    @DisplayName("DELETE /pets/{id} deve deletar pet")
    void deveDeletarPet() throws Exception {
        mockMvc.perform(delete("/pets/10"))
                .andExpect(status().isNoContent());

        verify(petService).delete(10L);
    }

    @Test
    @DisplayName("GET /pets deve listar todos os pets paginados")
    void deveListarTodosOsPets() throws Exception {
        Pet pet = criarPetMock(10L, "Thor");
        when(petService.findAll(any())).thenReturn(new PageImpl<>(List.of(pet), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Thor"));
    }

    @Test
    @DisplayName("GET /pets/by-usuario/{usuarioId} deve listar pets do usuario")
    void deveListarPetsPorUsuario() throws Exception {
        Pet pet = criarPetMock(10L, "Thor");
        when(petService.findByUsuario(eq(1L), any())).thenReturn(new PageImpl<>(List.of(pet), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/pets/by-usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Thor"));
    }
}
