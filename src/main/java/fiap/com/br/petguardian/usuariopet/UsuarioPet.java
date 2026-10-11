package fiap.com.br.petguardian.usuariopet;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_pet")
@NamedStoredProcedureQuery(
        name = "UsuarioPet.obterResumoRedeCuidado",
        procedureName = "pkg_petguardian.pr_obter_resumo_rede_cuidado",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_id_usuario", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_total_pets", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_total_cuidadores", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_tarefas_pendentes", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_tarefas_concluidas", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_pontos_acumulados", type = Integer.class)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
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
