package fiap.com.br.petguardian.pet.raca;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RacaRepository extends JpaRepository<Raca, Long> {
    Optional<Raca> findByNomeIgnoreCase(String nome);
}
