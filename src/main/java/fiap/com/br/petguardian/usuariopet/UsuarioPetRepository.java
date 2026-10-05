package fiap.com.br.petguardian.usuariopet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UsuarioPetRepository extends JpaRepository<UsuarioPet, UsuarioPetId> {

    @Query("select case when count(up) > 0 then true else false end from UsuarioPet up where lower(up.usuario.email) = lower(:email) and up.pet.id = :petId and up.responsavelPrincipal = true")
    boolean isResponsavelPrincipalPorEmail(@Param("email") String email, @Param("petId") Long petId);

    @Query("select case when count(up) > 0 then true else false end from UsuarioPet up where lower(up.usuario.email) = lower(:email) and up.pet.id = :petId")
    boolean existsByUsuarioEmailAndPetId(@Param("email") String email, @Param("petId") Long petId);

    @Query("select up from UsuarioPet up join fetch up.usuario u join fetch up.pet p where lower(up.usuario.email) = lower(:email) and up.pet.id = :petId")
    Optional<UsuarioPet> findByUsuarioEmailAndPetId(@Param("email") String email, @Param("petId") Long petId);

    @Query("select up from UsuarioPet up join fetch up.pet p join fetch p.raca r join fetch up.usuario u where lower(up.usuario.email) = lower(:email)")
    List<UsuarioPet> findAllByUsuarioEmail(@Param("email") String email);

    @Query("select up from UsuarioPet up join fetch up.usuario u join fetch up.pet p where up.pet.id = :petId")
    List<UsuarioPet> findAllByPetId(@Param("petId") Long petId);

    @Query("select up from UsuarioPet up join fetch up.pet p join fetch up.usuario u where up.pet.id in :petIds")
    List<UsuarioPet> findAllByPetIdIn(@Param("petIds") List<Long> petIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update UsuarioPet up set up.responsavelPrincipal = false where up.pet.id = :petId")
    void limparResponsavelPrincipalPorPet(@Param("petId") Long petId);
}
