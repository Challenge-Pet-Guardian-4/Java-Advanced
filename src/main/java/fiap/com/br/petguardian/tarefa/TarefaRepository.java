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

    @EntityGraph(attributePaths = {"status", "pet"})
    Page<Tarefa> findAllByPetUsuarioPetsUsuarioIdAndStatusNomeStatus(Long usuarioId, EnumStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"status", "pet"})
    Page<Tarefa> findAllByPetUsuarioPetsUsuarioId(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"status", "pet"})
    Page<Tarefa> findAllByPetId(Long petId, Pageable pageable);

    @Query("select coalesce(sum(t.pontosTarefa), 0) from Tarefa t where t.usuario.id = :usuarioId and t.status.nomeStatus = :status")
    Integer calcularPontosTotaisUsuario(@Param("usuarioId") Long usuarioId, @Param("status") EnumStatus status);

    @EntityGraph(attributePaths = {"pet"})
    List<Tarefa> findAllByPetIdInAndStatusNomeStatusOrderByConclusaoDesc(List<Long> petIds, EnumStatus status);

    @Query("select t.pet.id, t.id from Tarefa t where t.pet.id in :petIds")
    List<Object[]> findTarefaIdsByPetIdIn(@Param("petIds") List<Long> petIds);

    int countByPetIdInAndStatusNomeStatus(List<Long> petIds, EnumStatus status);

    int countByPetIdInAndStatusNomeStatusAndPrazoGreaterThanEqual(List<Long> petIds, EnumStatus status, LocalDateTime agora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Tarefa t set t.status = :expirada where t.status = :pendente and t.prazo < :agora")
    void expirarTarefasPendentesAtrasadas(
            @Param("agora") LocalDateTime agora,
            @Param("pendente") Status pendente,
            @Param("expirada") Status expirada
    );
}
