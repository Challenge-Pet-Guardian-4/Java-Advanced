package fiap.com.br.petguardian.trilha.modulo;

import fiap.com.br.petguardian.trilha.Trilha;
import fiap.com.br.petguardian.trilha.aula.Aula;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "modulo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modulo")
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(name = "tempo_conclusao", nullable = false, length = 10)
    private String tempoConclusao;

    @Column(nullable = false, length = 100)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trilha_id_trilha", nullable = false)
    private Trilha trilha;

    @OneToMany(mappedBy = "modulo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Aula> aulas = new HashSet<>();
}
