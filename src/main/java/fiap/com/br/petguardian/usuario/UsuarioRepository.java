package fiap.com.br.petguardian.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Override
    @EntityGraph(attributePaths = {"telefone"})
    Page<Usuario> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"telefone"})
    Optional<Usuario> findById(Long id);

    @EntityGraph(attributePaths = {"telefone"})
    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @EntityGraph(attributePaths = {"telefone"})
    Optional<Usuario> findByEmailIgnoreCase(String email);
}
