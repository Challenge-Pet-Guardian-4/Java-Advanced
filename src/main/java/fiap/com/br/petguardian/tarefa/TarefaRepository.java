package fiap.com.br.petguardian.tarefa;

import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.tarefa.status.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    @Override
    @EntityGraph(attributePaths = {"status", "pet"})
    Page<Tarefa> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"status", "pet"})
    Optional<Tarefa> findById(Long id);

    @Query("select t from Tarefa t " +
            "join fetch t.status " +
            "join fetch t.pet p " +
            "join p.usuarioPets up " +
            "where t.id = :id and up.usuario.id = :usuarioId")
    Optional<Tarefa> findByIdAndUsuarioId(@Param("id") Long id, @Param("usuarioId") Long usuarioId);

    @Query(value = "select t from Tarefa t " +
            "join fetch t.status s " +
            "join fetch t.pet p " +
            "join p.usuarioPets up " +
            "where up.usuario.id = :usuarioId " +
            "and s.nomeStatus = :status",
           countQuery = "select count(t) from Tarefa t " +
            "join t.pet p " +
            "join p.usuarioPets up " +
            "where up.usuario.id = :usuarioId " +
            "and t.status.nomeStatus = :status")
    Page<Tarefa> findAllDoCuidadorByStatus(
            @Param("usuarioId") Long usuarioId,
            @Param("status") EnumStatus status,
            Pageable pageable);

    @Query(value = "select t from Tarefa t " +
            "join fetch t.status " +
            "join fetch t.pet p " +
            "join p.usuarioPets up " +
            "where up.usuario.id = :usuarioId",
           countQuery = "select count(t) from Tarefa t " +
            "join t.pet p " +
            "join p.usuarioPets up " +
            "where up.usuario.id = :usuarioId")
    Page<Tarefa> findAllDoCuidador(
            @Param("usuarioId") Long usuarioId,
            Pageable pageable);

    @Query(value = "select t from Tarefa t " +
            "join fetch t.status " +
            "join fetch t.pet " +
            "where t.pet.id = :petId",
           countQuery = "select count(t) from Tarefa t " +
            "where t.pet.id = :petId")
    Page<Tarefa> findAllByPetId(
            @Param("petId") Long petId,
            Pageable pageable);

    @Query("select coalesce(sum(t.pontosTarefa), 0) from Tarefa t " +
            "where t.usuario.id = :usuarioId " +
            "and t.status.nomeStatus = :status")
    Integer calcularPontosTotaisUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("status") EnumStatus status);

    @Query("select coalesce(sum(t.pontosTarefa), 0) from Tarefa t " +
            "where t.pet.id = :petId " +
            "and t.status.nomeStatus = :status")
    Integer calcularPontosTarefasPorPet(
            @Param("petId") Long petId,
            @Param("status") EnumStatus status);

    @Query("select t from Tarefa t where t.pet.id = :petId and t.status.nomeStatus = :status order by t.conclusao desc")
    List<Tarefa> findConcluidasByPetId(
            @Param("petId") Long petId,
            @Param("status") EnumStatus status);

    @Query("select t.pet.id, t.id from Tarefa t where t.pet.id in :petIds")
    List<Object[]> findTarefaIdsByPetIdIn(@Param("petIds") List<Long> petIds);

    @Query("select count(t) from Tarefa t where t.pet.id in :petIds and t.status.nomeStatus = :status")
    int countByPetIdInAndStatus(
            @Param("petIds") List<Long> petIds,
            @Param("status") EnumStatus status);

    @Query("select count(t) from Tarefa t where t.pet.id in :petIds and t.status.nomeStatus = :status and t.prazo >= :agora")
    int countByPetIdInAndStatusAndPrazoFuturo(
            @Param("petIds") List<Long> petIds,
            @Param("status") EnumStatus status,
            @Param("agora") LocalDateTime agora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Tarefa t set t.status = :expirada where t.status = :pendente and t.prazo < :agora")
    void expirarTarefasPendentesAtrasadas(
            @Param("agora") LocalDateTime agora,
            @Param("pendente") Status pendente,
            @Param("expirada") Status expirada
    );
}
