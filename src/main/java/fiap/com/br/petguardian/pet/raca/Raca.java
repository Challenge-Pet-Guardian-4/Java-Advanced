package fiap.com.br.petguardian.pet.raca;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "raca")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Raca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_raca")
    private Long id;

    @Column(name = "nome_raca", nullable = false, length = 30, unique = true)
    private String nome;
}
