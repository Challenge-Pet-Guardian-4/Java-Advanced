package fiap.com.br.petguardian.usuariopet;

import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.dto.RedeCuidadoResponse;
import fiap.com.br.petguardian.usuario.dto.RedeCuidadoResponse.CuidadorResumo;
import fiap.com.br.petguardian.usuario.dto.RedeCuidadoResponse.PetResumo;
import fiap.com.br.petguardian.util.DbUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Componente de mapeamento dedicado para a Rede de Cuidado.
 * Isola a manipulação e agrupamento de coleções relacionais, mantendo o UsuarioPetService
 * limpo e focado estritamente nas regras de negócio e segurança.
 */
@Component
public class RedeCuidadoMapper {

    /**
     * Resposta padrão caso o tutor ainda não possua nenhum pet cadastrado.
     * Retorna listas vazias, contadores zerados e nível inicial "BRONZE".
     */
    public RedeCuidadoResponse toEmptyResponse(Usuario usuario) {
        return new RedeCuidadoResponse(
                usuario.getEmail(),
                usuario.getNome(),
                List.of(),
                List.of(),
                0,
                0,
                0,
                "BRONZE"
        );
    }

    /**
     * Mapeia os pets do tutor associando o total de tarefas pendentes e concluídas de cada um.
     */
    public List<PetResumo> toPetResumoList(List<UsuarioPet> vinculos, Map<Long, Integer> totalTarefasPorPet) {
        return vinculos.stream()
                .map(v -> new PetResumo(
                        v.getPet().getId(),
                        v.getPet().getNome(),
                        v.getPet().getRaca().getNome(),
                        v.isResponsavelPrincipal(),
                        totalTarefasPorPet.getOrDefault(v.getPet().getId(), 0)
                ))
                .toList();
    }

    /**
     * Agrupa e mapeia os co-cuidadores que compartilham pets na família.
     * Separa os pets onde o cuidador é titular daqueles onde atua como apoio.
     */
    public List<CuidadorResumo> toCuidadorResumoList(List<UsuarioPet> todosVinculos, String emailLogado) {
        // Agrupa os vínculos por ID do usuário cuidador (ignorando o tutor logado)
        Map<Long, List<UsuarioPet>> vinculosPorCuidador = todosVinculos.stream()
                .filter(v -> !v.getUsuario().getEmail().equalsIgnoreCase(emailLogado))
                .collect(Collectors.groupingBy(v -> v.getUsuario().getId()));

        return vinculosPorCuidador.values().stream()
                .map(vinculosDoCuidador -> {
                    Usuario cuidador = vinculosDoCuidador.get(0).getUsuario();

                    List<Long> petIds = new ArrayList<>();
                    List<String> petNomes = new ArrayList<>();
                    List<String> petsPrincipalNomes = new ArrayList<>();
                    List<String> petsAjudaNomes = new ArrayList<>();

                    // Em um único loop, separa os pets onde ele é titular dos de apoio
                    for (UsuarioPet v : vinculosDoCuidador) {
                        petIds.add(v.getPet().getId());
                        petNomes.add(v.getPet().getNome());
                        if (v.isResponsavelPrincipal()) {
                            petsPrincipalNomes.add(v.getPet().getNome());
                        } else {
                            petsAjudaNomes.add(v.getPet().getNome());
                        }
                    }

                    return new CuidadorResumo(
                            cuidador.getNome(),
                            cuidador.getEmail(),
                            !petsPrincipalNomes.isEmpty(), // É titular se tiver ao menos 1 pet principal
                            petIds,
                            petNomes,
                            petsPrincipalNomes,
                            petsAjudaNomes
                    );
                })
                .toList();
    }

    /**
     * Constrói o DTO final combinando dados relacionais com as saídas das Stored Procedures e Functions do Oracle.
     */
    public RedeCuidadoResponse toResponse(
            Usuario usuario,
            List<PetResumo> pets,
            List<CuidadorResumo> cuidadores,
            Map<String, Object> resumoDb,
            String nivel
    ) {
        return new RedeCuidadoResponse(
                usuario.getEmail(),
                usuario.getNome(),
                pets,
                cuidadores,
                DbUtils.getInt(resumoDb, "p_tarefas_pendentes"),
                DbUtils.getInt(resumoDb, "p_tarefas_concluidas"),
                DbUtils.getInt(resumoDb, "p_pontos_acumulados"),
                nivel != null ? nivel : "BRONZE"
        );
    }
}
