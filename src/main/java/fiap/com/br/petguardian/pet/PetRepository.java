package fiap.com.br.petguardian.pet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    @Override
    @EntityGraph(attributePaths = {"raca"})
    Page<Pet> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"raca"})
    Optional<Pet> findById(Long id);

    @EntityGraph(attributePaths = {"raca"})
    Page<Pet> findByUsuarioPetsUsuarioId(Long usuarioId, Pageable pageable);

    @Procedure(name = "Pet.calcularPontuacaoPet")
    Map<String, Object> calcularPontuacaoPetNoBanco(@Param("p_id_pet") Long petId);

    @Procedure(name = "Pet.calcularPontuacaoUsuario")
    Map<String, Object> calcularPontuacaoUsuarioNoBanco(@Param("p_id_usuario") Long usuarioId);
}
