package fiap.com.br.petguardian.usuariopet;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UsuarioPetRepository extends JpaRepository<UsuarioPet, UsuarioPetId> {

    boolean existsByUsuarioEmailIgnoreCaseAndPetIdAndResponsavelPrincipalTrue(String email, Long petId);

    boolean existsByUsuarioEmailIgnoreCaseAndPetId(String email, Long petId);

    @EntityGraph(attributePaths = {"usuario", "pet"})
    Optional<UsuarioPet> findByUsuarioEmailIgnoreCaseAndPetId(String email, Long petId);

    @EntityGraph(attributePaths = {"pet.raca", "usuario"})
    List<UsuarioPet> findAllByUsuarioEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"usuario", "pet"})
    List<UsuarioPet> findAllByPetId(Long petId);

    @EntityGraph(attributePaths = {"usuario", "pet"})
    List<UsuarioPet> findAllByPetIdIn(List<Long> petIds);

    @Procedure(procedureName = "pkg_petguardian.pr_transferir_responsavel_principal")
    void transferirResponsavelPrincipalNoBanco(@Param("p_id_pet") Long petId, @Param("p_novo_id_usuario") Long novoIdUsuario);

    @Procedure(procedureName = "pkg_petguardian.pr_obter_resumo_rede_cuidado")
    Map<String, Object> obterResumoRedeCuidadoNoBanco(@Param("p_id_usuario") Long usuarioId);
}
