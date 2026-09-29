package fiap.com.br.petguardian.endereco.bairro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BairroRepository extends JpaRepository<Bairro, Long> {
    Optional<Bairro> findByNomeIgnoreCaseAndCidadeId(String nome, Long cidadeId);
}
