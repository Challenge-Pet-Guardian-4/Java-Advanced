package fiap.com.br.petguardian.pet;

import fiap.com.br.petguardian.pet.raca.Raca;
import fiap.com.br.petguardian.tarefa.Tarefa;
import fiap.com.br.petguardian.usuariopet.UsuarioPet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pet")
    private Long id;

    @Column(nullable = false, length = 30)
    private String nome;

    @Column(name = "data_nasc", nullable = false)
    private LocalDate dataNasc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raca_id_raca", nullable = false)
    private Raca raca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PetPorte porte;

    @Column(nullable = false, length = 1)
    private Character sexo;

    @Column(nullable = false)
    @Builder.Default
    private boolean castrado = false;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Tarefa> tarefas = new HashSet<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UsuarioPet> usuarioPets = new HashSet<>();
}
