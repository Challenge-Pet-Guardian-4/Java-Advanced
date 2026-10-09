package fiap.com.br.petguardian.pet.historico;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    @Override
    @EntityGraph(attributePaths = {"pet"})
    Optional<Historico> findById(Long id);

    @EntityGraph(attributePaths = {"pet"})
    List<Historico> findAllByPetIdOrderByDataHistDesc(Long petId);

    @EntityGraph(attributePaths = {"pet"})
    @Query("select h from Historico h join h.pet p join p.usuarioPets up where lower(up.usuario.email) = lower(:email) order by h.dataHist desc")
    List<Historico> findAllByUsuarioEmailOrderByDataHistDesc(@Param("email") String email);
}
