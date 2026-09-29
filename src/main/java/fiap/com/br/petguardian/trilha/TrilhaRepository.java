package fiap.com.br.petguardian.trilha;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrilhaRepository extends JpaRepository<Trilha, Long> {
    @EntityGraph(attributePaths = {"pet"})
    List<Trilha> findAllByPetId(Long petId);
    
    boolean existsByNomeIgnoreCaseAndPetId(String nome, Long petId);

    boolean existsByNomeIgnoreCaseAndPetIdAndIdNot(String nome, Long petId, Long id);
}
