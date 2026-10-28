package fiap.com.br.petguardian.endereco;

import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.com.br.petguardian.endereco.bairro.Bairro;
import fiap.com.br.petguardian.endereco.cidade.Cidade;
import fiap.com.br.petguardian.endereco.dto.EnderecoRequest;
import fiap.com.br.petguardian.endereco.estado.Estado;
import fiap.com.br.petguardian.exception.GlobalExceptionHandler;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EnderecoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EnderecoService enderecoService;

    @InjectMocks
    private EnderecoController enderecoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(enderecoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private Endereco criarEnderecoMock(Long id) {
        Estado estado = Estado.builder().id(1L).nome("SP").build();
        Cidade cidade = Cidade.builder().id(1L).nome("São Paulo").estado(estado).build();
        Bairro bairro = Bairro.builder().id(1L).nome("Bela Vista").cidade(cidade).build();
        return Endereco.builder()
                .id(id)
                .cep("01310100")
                .numero("100")
                .rua("Av Paulista")
                .bairro(bairro)
                .build();
    }

    @Test
    @DisplayName("GET /enderecos deve listar enderecos")
    void deveListarEnderecos() throws Exception {
        Endereco endereco = criarEnderecoMock(1L);
        when(enderecoService.findAll(any())).thenReturn(new PageImpl<>(List.of(endereco), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/enderecos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cep").value("01310100"));
    }

    @Test
    @DisplayName("GET /enderecos/{id} deve buscar endereco por id")
    void deveBuscarEnderecoPorId() throws Exception {
        Endereco endereco = criarEnderecoMock(1L);
        when(enderecoService.findById(1L)).thenReturn(endereco);

        mockMvc.perform(get("/enderecos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01310100"));
    }

    @Test
    @DisplayName("POST /enderecos deve criar endereco")
    void deveCriarEndereco() throws Exception {
        var request = new EnderecoRequest("01310100", "100");
        Endereco endereco = criarEnderecoMock(1L);
        when(enderecoService.create(any())).thenReturn(endereco);

        mockMvc.perform(post("/enderecos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /enderecos/{id} deve atualizar endereco")
    void deveAtualizarEndereco() throws Exception {
        var request = new EnderecoRequest("01310100", "200");
        Endereco endereco = criarEnderecoMock(1L);
        when(enderecoService.update(eq(1L), any())).thenReturn(endereco);

        mockMvc.perform(put("/enderecos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /enderecos/{id} deve deletar endereco")
    void deveDeletarEndereco() throws Exception {
        mockMvc.perform(delete("/enderecos/1"))
                .andExpect(status().isNoContent());

        verify(enderecoService).delete(1L);
    }
}
