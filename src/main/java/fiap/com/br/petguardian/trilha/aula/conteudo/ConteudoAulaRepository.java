package fiap.com.br.petguardian.trilha.aula.conteudo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ConteudoAulaRepository extends MongoRepository<ConteudoAula, String> {

    Optional<ConteudoAula> findByAulaId(Long aulaId);

    void deleteByAulaId(Long aulaId);
}
