package fiap.com.br.petguardian.endereco.cidade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CidadeRepository extends JpaRepository<Cidade, Long> {
    Optional<Cidade> findByNomeIgnoreCaseAndEstadoId(String nome, Long estadoId);
}
