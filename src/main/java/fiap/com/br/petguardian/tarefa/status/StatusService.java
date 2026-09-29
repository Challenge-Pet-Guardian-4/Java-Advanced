package fiap.com.br.petguardian.tarefa.status;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusService {
    private final StatusRepository statusRepository;

    @Cacheable(value = "status", key = "#status.name()")
    public Status findStatus(EnumStatus status) {
        return statusRepository.findByNomeStatus(status)
                .orElseThrow(() -> new ResourceNotFoundException("Status '" + status + "' não encontrado."));
    }

    public Status findStatusByNome(String nome) {
        return findStatus(EnumStatus.valueOf(nome.trim().toUpperCase()));
    }
}
