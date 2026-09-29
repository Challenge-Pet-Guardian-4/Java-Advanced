package fiap.com.br.petguardian.usuariopet;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_pet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioPet {

    @EmbeddedId
    private UsuarioPetId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("petId")
    @JoinColumn(name = "pet_id_pet", nullable = false)
    private Pet pet;

    @Builder.Default
    @Column(name = "respon_princ", nullable = false)
    private boolean responsavelPrincipal = false;

    public void promoverResponsavelPrincipal() {
        this.responsavelPrincipal = true;
    }
}
