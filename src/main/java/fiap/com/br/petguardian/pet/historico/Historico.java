package fiap.com.br.petguardian.pet.historico;

import fiap.com.br.petguardian.pet.Pet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_hist")
    private Long id;

    @Column(name = "tipo_hist", nullable = false, length = 30)
    private String tipoHist;

    @Column(name = "data_hist", nullable = false)
    private LocalDateTime dataHist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id_pet", nullable = false)
    private Pet pet;
}
