package fiap.com.br.petguardian.trilha.modulo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    @Override
    @EntityGraph(attributePaths = {"trilha"})
    Optional<Modulo> findById(Long id);

    @EntityGraph(attributePaths = {"trilha"})
    List<Modulo> findAllByTrilhaId(Long trilhaId);
}
