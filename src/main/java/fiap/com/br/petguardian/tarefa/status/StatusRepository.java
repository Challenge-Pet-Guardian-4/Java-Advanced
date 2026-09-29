package fiap.com.br.petguardian.tarefa.status;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StatusRepository extends JpaRepository<Status, Long> {
    Optional<Status> findByNomeStatus(EnumStatus nomeStatus);
}
