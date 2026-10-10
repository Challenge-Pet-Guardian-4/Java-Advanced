package fiap.com.br.petguardian.pet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Page<Pet> findAll(Pageable pageable);

    Optional<Pet> findById(Long id);

    Page<Pet> findByUsuarioId(Long usuarioId, Pageable pageable);

    @Procedure(procedureName = "pkg_petguardian.pr_calcular_pontuacao_pet")
    Map<String, Object> calcularPontuacaoPetNoBanco(@Param("p_id_pet") Long petId);

    @Procedure(procedureName = "pkg_petguardian.pr_calcular_pontuacao_usuario")
    Map<String, Object> calcularPontuacaoUsuarioNoBanco(@Param("p_id_usuario") Long usuarioId);
}
