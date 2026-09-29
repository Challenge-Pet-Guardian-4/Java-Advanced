package fiap.com.br.petguardian.tarefa.status;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nome_status", nullable = false, length = 15, unique = true)
    private EnumStatus nomeStatus;
}
