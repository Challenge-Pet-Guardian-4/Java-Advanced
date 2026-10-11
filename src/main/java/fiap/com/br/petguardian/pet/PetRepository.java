package fiap.com.br.petguardian.pet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Map;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Page<Pet> findByUsuarioPetsUsuarioId(Long usuarioId, Pageable pageable);

    @Procedure(name = "Pet.calcularPontuacaoPet")
    Map<String, Object> calcularPontuacaoPetNoBanco(@Param("p_id_pet") Long petId);

    @Procedure(name = "Pet.calcularPontuacaoUsuario")
    Map<String, Object> calcularPontuacaoUsuarioNoBanco(@Param("p_id_usuario") Long usuarioId);
}
