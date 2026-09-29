package fiap.com.br.petguardian.trilha;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.trilha.modulo.Modulo;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trilha")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trilha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trilha")
    private Long id;

    @Column(nullable = false, length = 30)
    private String nome;

    @Column(nullable = false, length = 200)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id_pet", nullable = false)
    private Pet pet;

    @OneToMany(mappedBy = "trilha", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Modulo> modulos = new HashSet<>();
}
