package fiap.com.br.petguardian.trilha;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrilhaRepository extends JpaRepository<Trilha, Long> {

    @Override
    @EntityGraph(attributePaths = {"pet"})
    Page<Trilha> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"pet"})
    Optional<Trilha> findById(Long id);

    @EntityGraph(attributePaths = {"pet"})
    List<Trilha> findAllByPetId(Long petId);

    @EntityGraph(attributePaths = {"pet", "modulos", "modulos.aulas"})
    @Query("select distinct t from Trilha t join t.pet p join p.usuarioPets up where lower(up.usuario.email) = lower(:email) order by t.nome asc")
    List<Trilha> findAllByUsuarioEmail(@Param("email") String email);
}
