package fiap.com.br.petguardian.pet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    @Override
    @EntityGraph(attributePaths = {"raca"})
    Page<Pet> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"raca"})
    Optional<Pet> findById(Long id);

    Page<Pet> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query(value = "select p from Pet p join fetch p.raca r join p.usuarioPets up where up.usuario.id = :usuarioId",
           countQuery = "select count(p) from Pet p join p.usuarioPets up where up.usuario.id = :usuarioId")
    Page<Pet> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);
}
