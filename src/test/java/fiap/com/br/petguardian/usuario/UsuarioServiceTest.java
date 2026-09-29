package fiap.com.br.petguardian.usuario;

import fiap.com.br.petguardian.endereco.Endereco;
import fiap.com.br.petguardian.endereco.EnderecoService;
import fiap.com.br.petguardian.endereco.dto.EnderecoRequest;
import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.telefone.Telefone;
import fiap.com.br.petguardian.telefone.TelefoneRepository;
import fiap.com.br.petguardian.usuario.dto.RedeCuidadoResponse;
import fiap.com.br.petguardian.usuario.dto.UsuarioRequest;
import fiap.com.br.petguardian.usuariopet.UsuarioPetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EnderecoService enderecoService;

    @Mock
    private TelefoneRepository telefoneRepository;

    @Mock
    private UsuarioPetService usuarioPetService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve listar todos os usuarios paginados")
    void deveListarTodosUsuarios() {
        Pageable pageable = PageRequest.of(0, 10);
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").build();
        when(usuarioRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(usuario)));

        Page<Usuario> resultado = usuarioService.findAll(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(usuarioRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar usuario por ID com sucesso")
    void deveBuscarUsuarioPorId() {
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.findById(1L);

        assertNotNull(resultado);
        assertEquals("Enzo", resultado.getNome());
    }

    @Test
    @DisplayName("Deve lancar excecao ao buscar ID inexistente")
    void deveLancarExcecaoIdInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.findById(99L));
    }

    @Test
    @DisplayName("Deve criar usuario com telefone e endereco resolvido")
    void deveCriarUsuarioComSucesso() {
        var enderecoReq = new EnderecoRequest("01310100", "100");
        var request = new UsuarioRequest("Enzo", "enzo@fiap.com.br", "123456", "11", "987654321", "PREMIUM", enderecoReq);

        Endereco endereco = Endereco.builder().id(1L).cep("01310100").numero("100").build();
        Telefone telefone = Telefone.builder().id(1L).ddd("11").numero("987654321").build();
        Usuario usuarioSalvo = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").role(UsuarioRole.PREMIUM).enderecos(new HashSet<>()).build();

        when(enderecoService.findOrCreateByCepAndNumero(enderecoReq)).thenReturn(endereco);
        when(telefoneRepository.save(any(Telefone.class))).thenReturn(telefone);
        when(passwordEncoder.encode("123456")).thenReturn("hashedPwd");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        Usuario resultado = usuarioService.create(request);

        assertNotNull(resultado);
        assertEquals("Enzo", resultado.getNome());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve deletar usuario existente")
    void deveDeletarUsuario() {
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.delete(1L);

        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve retornar rede de cuidado do usuario")
    void deveRetornarRedeDeCuidado() {
        var redeResponse = new RedeCuidadoResponse(1L, "Enzo", List.of(), List.of(), 0, 0, 0);
        when(usuarioPetService.montarRedeCuidado(1L)).thenReturn(redeResponse);

        RedeCuidadoResponse resultado = usuarioService.getRedeCuidado(1L);

        assertNotNull(resultado);
        assertEquals("Enzo", resultado.nomeUsuario());
    }
}
