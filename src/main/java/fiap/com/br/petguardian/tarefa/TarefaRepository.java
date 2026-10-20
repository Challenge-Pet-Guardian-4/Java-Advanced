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
    @EntityGraph(attributePaths = {"status", "pet", "usuario"})
    Page<Tarefa> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"status", "pet", "usuario"})
    Optional<Tarefa> findById(Long id);

    @EntityGraph(attributePaths = {"status", "pet", "usuario"})
    Page<Tarefa> findAllByPetUsuarioPetsUsuarioIdAndStatusNomeStatus(Long usuarioId, EnumStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"status", "pet", "usuario"})
    Page<Tarefa> findAllByPetUsuarioPetsUsuarioId(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"status", "pet", "usuario"})
    Page<Tarefa> findAllByPetId(Long petId, Pageable pageable);

    @EntityGraph(attributePaths = {"pet"})
    List<Tarefa> findAllByPetIdInAndStatusNomeStatusOrderByConclusaoDesc(List<Long> petIds, EnumStatus status);

    @Query("select t.pet.id, count(t) from Tarefa t where t.pet.id in :petIds group by t.pet.id")
    List<Object[]> countTarefasByPetIdIn(@Param("petIds") List<Long> petIds);

    @Modifying(clearAutomatically = false, flushAutomatically = true)
    @Transactional
    @Query("update Tarefa t set t.status = :expirada where t.status = :pendente and t.prazo < :agora")
    void expirarTarefasPendentesAtrasadas(
            @Param("agora") LocalDateTime agora,
            @Param("pendente") Status pendente,
            @Param("expirada") Status expirada
    );

    @EntityGraph(attributePaths = {"pet", "usuario"})
    List<Tarefa> findAllByStatusNomeStatusAndPrazoBetween(
            EnumStatus status,
            LocalDateTime inicio,
            LocalDateTime fim
    );
}
