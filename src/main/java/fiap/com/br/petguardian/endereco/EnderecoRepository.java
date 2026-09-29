package fiap.com.br.petguardian.endereco;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    @Override
    @EntityGraph(attributePaths = {"bairro.cidade.estado"})
    Page<Endereco> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"bairro.cidade.estado"})
    Optional<Endereco> findById(Long id);

    @EntityGraph(attributePaths = {"bairro.cidade.estado"})
    Optional<Endereco> findByCepAndNumeroAndBairroId(String cep, String numero, Long bairroId);
}
