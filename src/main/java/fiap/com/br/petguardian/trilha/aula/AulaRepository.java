package fiap.com.br.petguardian.trilha.aula;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AulaRepository extends JpaRepository<Aula, Long> {

    @Override
    @EntityGraph(attributePaths = {"modulo"})
    Optional<Aula> findById(Long id);

    @EntityGraph(attributePaths = {"modulo"})
    List<Aula> findAllByModuloId(Long moduloId);

    @Query("select coalesce(sum(a.pontosAula), 0) from Aula a " +
            "join a.modulo m " +
            "join m.trilha tr " +
            "where tr.pet.id = :petId and a.concluida = true")
    Integer calcularPontosAulasConcluidasPorPet(@Param("petId") Long petId);

    boolean existsByNomeIgnoreCaseAndModuloId(String nome, Long moduloId);

    boolean existsByNomeIgnoreCaseAndModuloIdAndIdNot(String nome, Long moduloId, Long id);
}
