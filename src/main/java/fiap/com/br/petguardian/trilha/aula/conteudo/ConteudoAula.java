package fiap.com.br.petguardian.trilha.aula.conteudo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "conteudos_aula")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConteudoAula {

    @Id
    private String id;

    @Indexed(unique = true)
    private Long aulaId;

    private String tipoConteudo;

    private String corpoMarkdown;

    private List<String> linksRecursos;

    private Instant atualizadoEm;
}
