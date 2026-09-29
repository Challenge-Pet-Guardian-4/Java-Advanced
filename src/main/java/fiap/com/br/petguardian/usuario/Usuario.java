package fiap.com.br.petguardian.usuario;

import fiap.com.br.petguardian.endereco.Endereco;
import fiap.com.br.petguardian.tarefa.Tarefa;
import fiap.com.br.petguardian.telefone.Telefone;
import fiap.com.br.petguardian.usuariopet.UsuarioPet;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(nullable = false, length = 60)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private UsuarioRole role = UsuarioRole.PREMIUM;

    @ManyToOne
    @JoinColumn(name = "telefone_id_telefone", nullable = false)
    private Telefone telefone;

    @ManyToMany
    @JoinTable(
        name = "usuario_endereco",
        joinColumns = @JoinColumn(name = "usuario_id_usuario"),
        inverseJoinColumns = @JoinColumn(name = "endereco_id_endereco")
    )
    @Builder.Default
    private Set<Endereco> enderecos = new HashSet<>();

    @OneToMany(mappedBy = "usuario")
    @Builder.Default
    private Set<Tarefa> tarefas = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UsuarioPet> usuarioPets = new HashSet<>();
}
