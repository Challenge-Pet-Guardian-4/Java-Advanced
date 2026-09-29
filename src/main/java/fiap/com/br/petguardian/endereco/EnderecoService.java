package fiap.com.br.petguardian.endereco;

import fiap.com.br.petguardian.endereco.bairro.Bairro;
import fiap.com.br.petguardian.endereco.bairro.BairroRepository;
import fiap.com.br.petguardian.endereco.cidade.Cidade;
import fiap.com.br.petguardian.endereco.cidade.CidadeRepository;
import fiap.com.br.petguardian.endereco.dto.EnderecoRequest;
import fiap.com.br.petguardian.endereco.dto.ViaCepResponse;
import fiap.com.br.petguardian.endereco.estado.Estado;
import fiap.com.br.petguardian.endereco.estado.EstadoRepository;
import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final EstadoRepository estadoRepository;
    private final CidadeRepository cidadeRepository;
    private final BairroRepository bairroRepository;
    private final ViaCepService viaCepService;

    public Page<Endereco> findAll(Pageable pageable) {
        return enderecoRepository.findAll(pageable);
    }

    public Endereco findById(Long id) {
        return findEnderecoById(id);
    }

    @Transactional
    public Endereco create(EnderecoRequest enderecoRequest) {
        return findOrCreateByCepAndNumero(enderecoRequest);
    }

    @Transactional
    public Endereco update(Long id, EnderecoRequest enderecoRequest) {
        Endereco endereco = findEnderecoById(id);
        aplicarEndereco(endereco, enderecoRequest);
        return enderecoRepository.save(endereco);
    }

    @Transactional
    public void delete(Long id) {
        findEnderecoById(id);
        enderecoRepository.deleteById(id);
    }

    private Endereco findEnderecoById(Long id) {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com id " + id + " não encontrado."));
    }

    @Transactional
    public Endereco findOrCreateByCepAndNumero(EnderecoRequest enderecoRequest) {
        Endereco novo = buildEndereco(enderecoRequest);
        return enderecoRepository
                .findByCepAndNumeroAndBairroId(novo.getCep(), novo.getNumero(), novo.getBairro().getId())
                .orElseGet(() -> enderecoRepository.save(novo));
    }

    private Endereco buildEndereco(EnderecoRequest request) {
        ViaCepResponse dados = obterDadosCep(request.cep());
        Bairro bairro = obterOuCriarBairro(dados);
        return request.toEntity(dados.logradouro(), bairro);
    }

    private Endereco aplicarEndereco(Endereco endereco, EnderecoRequest request) {
        ViaCepResponse dados = obterDadosCep(request.cep());
        Bairro bairro = obterOuCriarBairro(dados);
        return aplicarEm(endereco, request, dados.logradouro(), bairro);
    }

    private ViaCepResponse obterDadosCep(String cep) {
        ViaCepResponse dados = viaCepService.getEnderecoPorCep(cep.replaceAll("\\D", ""));
        if (Boolean.TRUE.equals(dados.erro())) {
            throw new IllegalArgumentException("CEP " + cep + " não encontrado.");
        }
        return dados;
    }

    private Bairro obterOuCriarBairro(ViaCepResponse dados) {
        Estado estado = estadoRepository.findByNomeIgnoreCase(dados.estado())
                .orElseGet(() -> estadoRepository.save(Estado.builder().nome(dados.estado()).build()));

        Cidade cidade = cidadeRepository.findByNomeIgnoreCaseAndEstadoId(dados.localidade(), estado.getId())
                .orElseGet(() -> cidadeRepository.save(Cidade.builder().nome(dados.localidade()).estado(estado).build()));

        return bairroRepository.findByNomeIgnoreCaseAndCidadeId(dados.bairro(), cidade.getId())
                .orElseGet(() -> bairroRepository.save(Bairro.builder().nome(dados.bairro()).cidade(cidade).build()));
    }

    private Endereco aplicarEm(Endereco endereco, EnderecoRequest request, String rua, Bairro bairro) {
        endereco.setCep(request.cep().replaceAll("\\D", ""));
        endereco.setNumero(request.numero());
        endereco.setRua(rua);
        endereco.setBairro(bairro);
        return endereco;
    }
}
