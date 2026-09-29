package fiap.com.br.petguardian.endereco;

import fiap.com.br.petguardian.endereco.bairro.BairroRepository;
import fiap.com.br.petguardian.endereco.cidade.CidadeRepository;
import fiap.com.br.petguardian.endereco.dto.EnderecoRequest;
import fiap.com.br.petguardian.endereco.dto.ViaCepResponse;
import fiap.com.br.petguardian.endereco.estado.EstadoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private EstadoRepository estadoRepository;

    @Mock
    private CidadeRepository cidadeRepository;

    @Mock
    private BairroRepository bairroRepository;

    @Mock
    private ViaCepService viaCepService;

    @InjectMocks
    private EnderecoService enderecoService;

    @Test
    @DisplayName("Deve listar todos os enderecos paginados")
    void deveListarEnderecos() {
        Pageable pageable = PageRequest.of(0, 10);
        Endereco endereco = Endereco.builder().id(1L).cep("01310100").numero("100").rua("Av Paulista").build();
        when(enderecoRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(endereco)));

        Page<Endereco> resultado = enderecoService.findAll(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("01310100", resultado.getContent().get(0).getCep());
    }

    @Test
    @DisplayName("Deve buscar endereco por ID com sucesso")
    void deveBuscarEnderecoPorId() {
        Endereco endereco = Endereco.builder().id(1L).cep("01310100").numero("100").rua("Av Paulista").build();
        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(endereco));

        Endereco resultado = enderecoService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("Deve deletar endereco")
    void deveDeletarEndereco() {
        Endereco endereco = Endereco.builder().id(1L).build();
        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(endereco));

        enderecoService.delete(1L);

        verify(enderecoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve atualizar endereco existente preservando colecao de usuarios")
    void deveAtualizarEnderecoPreservandoUsuarios() {
        Endereco enderecoExistente = Endereco.builder().id(1L).cep("01310100").numero("100").rua("Rua Antiga").build();
        var request = new EnderecoRequest("01310200", "200");
        var viaCep = new ViaCepResponse("Av Nova", "Bela Vista", "São Paulo", "SP", false);

        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(enderecoExistente));
        when(viaCepService.getEnderecoPorCep("01310200")).thenReturn(viaCep);
        when(estadoRepository.findByNomeIgnoreCase("SP")).thenReturn(Optional.empty());
        when(estadoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cidadeRepository.findByNomeIgnoreCaseAndEstadoId(any(), any())).thenReturn(Optional.empty());
        when(cidadeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bairroRepository.findByNomeIgnoreCaseAndCidadeId(any(), any())).thenReturn(Optional.empty());
        when(bairroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(enderecoRepository.save(any(Endereco.class))).thenAnswer(inv -> inv.getArgument(0));

        Endereco resultado = enderecoService.update(1L, request);

        assertNotNull(resultado);
        assertEquals("01310200", resultado.getCep());
        assertEquals("200", resultado.getNumero());
        assertEquals("Av Nova", resultado.getRua());
        assertEquals(enderecoExistente.getUsuarios(), resultado.getUsuarios());
    }
}
