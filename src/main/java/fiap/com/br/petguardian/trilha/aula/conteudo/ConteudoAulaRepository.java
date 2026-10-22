package fiap.com.br.petguardian.trilha.aula.conteudo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConteudoAulaRepository extends MongoRepository<ConteudoAula, String> {

    Optional<ConteudoAula> findByAulaId(Long aulaId);

    List<ConteudoAula> findByAulaIdIn(Collection<Long> aulaIds);

    void deleteByAulaId(Long aulaId);

    void deleteByAulaIdIn(Collection<Long> aulaIds);
}
