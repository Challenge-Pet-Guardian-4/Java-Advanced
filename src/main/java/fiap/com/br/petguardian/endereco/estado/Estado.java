package fiap.com.br.petguardian.endereco.estado;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fiap.com.br.petguardian.endereco.cidade.Cidade;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Long id;

    @Column(name = "nome_estado", nullable = false, length = 30)
    private String nome;

    @OneToMany(mappedBy = "estado")
    @JsonIgnore
    @Builder.Default
    private Set<Cidade> cidades = new HashSet<>();
}
