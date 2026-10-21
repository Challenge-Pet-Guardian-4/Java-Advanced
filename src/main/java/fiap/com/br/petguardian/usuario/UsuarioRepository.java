package fiap.com.br.petguardian.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Override
    @EntityGraph(attributePaths = {"telefone", "endereco.bairro.cidade.estado"})
    Page<Usuario> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"telefone", "endereco.bairro.cidade.estado"})
    Optional<Usuario> findById(Long id);

    @EntityGraph(attributePaths = {"telefone", "endereco.bairro.cidade.estado"})
    Optional<Usuario> findByEmail(String email);
}
