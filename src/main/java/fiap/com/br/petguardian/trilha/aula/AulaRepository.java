package fiap.com.br.petguardian.trilha.aula;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AulaRepository extends JpaRepository<Aula, Long> {

    @Override
    @EntityGraph(attributePaths = {"modulo"})
    Optional<Aula> findById(Long id);

    @EntityGraph(attributePaths = {"modulo"})
    List<Aula> findAllByModuloId(Long moduloId);

    boolean existsByNomeIgnoreCaseAndModuloId(String nome, Long moduloId);

    boolean existsByNomeIgnoreCaseAndModuloIdAndIdNot(String nome, Long moduloId, Long id);
}
