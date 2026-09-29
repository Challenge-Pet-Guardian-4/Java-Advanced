package fiap.com.br.petguardian.telefone;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "telefone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Telefone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_telefone")
    private Long id;

    @Column(name = "num_ddd", nullable = false, length = 2)
    private String ddd;

    @Column(name = "num_tel", nullable = false, length = 9)
    private String numero;
}
