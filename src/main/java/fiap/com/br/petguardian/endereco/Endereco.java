package fiap.com.br.petguardian.endereco;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fiap.com.br.petguardian.endereco.bairro.Bairro;
import fiap.com.br.petguardian.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "endereco")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Endereco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_endereco")
    private Long id;

    @Column(nullable = false, length = 8)
    private String cep;

    @Column(nullable = false, length = 5)
    private String numero;

    @Column(nullable = false, length = 150)
    private String rua;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bairro_id_bairro")
    private Bairro bairro;

    @ManyToMany(mappedBy = "enderecos")
    @JsonIgnore
    @Builder.Default
    private Set<Usuario> usuarios = new HashSet<>();
}
