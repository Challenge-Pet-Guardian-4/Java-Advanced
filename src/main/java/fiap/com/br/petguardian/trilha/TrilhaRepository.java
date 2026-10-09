package fiap.com.br.petguardian.trilha;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrilhaRepository extends JpaRepository<Trilha, Long> {
    @EntityGraph(attributePaths = {"pet"})
    List<Trilha> findAllByPetId(Long petId);

    @EntityGraph(attributePaths = {"pet"})
    @Query("select t from Trilha t join t.pet p join p.usuarioPets up where lower(up.usuario.email) = lower(:email) order by t.nome asc")
    List<Trilha> findAllByUsuarioEmail(@Param("email") String email);
    
    boolean existsByNomeIgnoreCaseAndPetId(String nome, Long petId);

    boolean existsByNomeIgnoreCaseAndPetIdAndIdNot(String nome, Long petId, Long id);
}
