package fiap.com.br.petguardian.trilha.aula;

import fiap.com.br.petguardian.trilha.modulo.Modulo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "aula")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aula")
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Column(name = "pontos_aula", nullable = false)
    private Integer pontosAula;

    @Column(nullable = false, length = 20)
    private String dificuldade;

    @Column(nullable = false, length = 1000)
    private String conteudo;

    @Column(nullable = false)
    @Builder.Default
    private boolean concluida = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id_modulo", nullable = false)
    private Modulo modulo;
}
