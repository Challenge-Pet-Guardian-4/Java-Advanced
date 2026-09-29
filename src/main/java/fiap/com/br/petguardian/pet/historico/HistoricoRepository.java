package fiap.com.br.petguardian.pet.historico;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    @Override
    @EntityGraph(attributePaths = {"pet"})
    Optional<Historico> findById(Long id);

    @EntityGraph(attributePaths = {"pet"})
    List<Historico> findAllByPetIdOrderByDataHistDesc(Long petId);
}
