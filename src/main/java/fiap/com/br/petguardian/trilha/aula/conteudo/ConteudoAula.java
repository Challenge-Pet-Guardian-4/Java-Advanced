package fiap.com.br.petguardian.trilha.aula.conteudo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @Indexed(name = "idx_aula_id_unique", unique = true)
    @Field("aula_id")
    private Long aulaId;

    @Field("tipo_conteudo")
    private String tipoConteudo;

    @Field("corpo_markdown")
    private String corpoMarkdown;

    @Field("links_recursos")
    private List<String> linksRecursos;

    @Field("atualizado_em")
    private Instant atualizadoEm;
}
