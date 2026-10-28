package fiap.com.br.petguardian.usuario;

import fiap.com.br.petguardian.endereco.Endereco;
import fiap.com.br.petguardian.endereco.EnderecoService;
import fiap.com.br.petguardian.endereco.dto.EnderecoRequest;
import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetService;
import fiap.com.br.petguardian.telefone.Telefone;
import fiap.com.br.petguardian.telefone.TelefoneRepository;
import fiap.com.br.petguardian.usuario.dto.UsuarioRequest;
import fiap.com.br.petguardian.usuariopet.UsuarioPet;
import fiap.com.br.petguardian.usuariopet.UsuarioPetId;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
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
    private PasswordEncoder passwordEncoder;

    @Mock
    private PetService petService;

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
        var request = new UsuarioRequest("Enzo", "enzo@fiap.com.br", "123456", "11", "987654321", enderecoReq);

        Endereco endereco = Endereco.builder().id(1L).cep("01310100").numero("100").build();
        Telefone telefone = Telefone.builder().id(1L).ddd("11").numero("987654321").build();
        Usuario usuarioSalvo = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").role(UsuarioRole.COMUM).endereco(endereco).build();

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
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.delete(1L);

        verify(usuarioRepository).delete(usuario);
    }

    @Test
    @DisplayName("Deve buscar usuario por email com sucesso")
    void deveBuscarUsuarioPorEmail() {
        Usuario usuario = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").build();
        when(usuarioRepository.findByEmail("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.findByEmail("enzo@fiap.com.br");

        assertNotNull(resultado);
        assertEquals("enzo@fiap.com.br", resultado.getEmail());
    }

    @Test
    @DisplayName("Deve criar usuario sempre com role COMUM independente de qualquer input")
    void deveCriarUsuarioSempreComoComum() {
        var enderecoReq = new EnderecoRequest("01310100", "100");
        var request = new UsuarioRequest("Enzo", "enzo@fiap.com.br", "123456", "11", "987654321", enderecoReq);

        Endereco endereco = Endereco.builder().id(1L).cep("01310100").numero("100").build();
        Telefone telefone = Telefone.builder().id(1L).ddd("11").numero("987654321").build();
        Usuario usuarioSalvo = Usuario.builder().id(1L).nome("Enzo").email("enzo@fiap.com.br").role(UsuarioRole.COMUM).endereco(endereco).build();

        when(enderecoService.findOrCreateByCepAndNumero(enderecoReq)).thenReturn(endereco);
        when(telefoneRepository.save(any(Telefone.class))).thenReturn(telefone);
        when(passwordEncoder.encode("123456")).thenReturn("hashedPwd");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        Usuario resultado = usuarioService.create(request);

        assertEquals(UsuarioRole.COMUM, resultado.getRole());
    }

    @Test
    @DisplayName("Deve realizar upgrade de COMUM para PREMIUM com sucesso")
    void deveRealizarUpgradePremium() {
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").role(UsuarioRole.COMUM).build();
        when(usuarioRepository.findByEmail("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.upgradePremium("enzo@fiap.com.br");

        assertEquals(UsuarioRole.PREMIUM, resultado.getRole());
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar upgrade de usuario que ja eh PREMIUM")
    void deveLancarExcecaoUpgradeJaPremium() {
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").role(UsuarioRole.PREMIUM).build();
        when(usuarioRepository.findByEmail("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.upgradePremium("enzo@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar upgrade de usuario ADMIN")
    void deveLancarExcecaoUpgradeAdmin() {
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").role(UsuarioRole.ADMIN).build();
        when(usuarioRepository.findByEmail("enzo@fiap.com.br")).thenReturn(Optional.of(usuario));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.upgradePremium("enzo@fiap.com.br"));
    }

    @Test
    @DisplayName("Deve atualizar role do usuario quando chamado por ADMIN")
    void deveAtualizarRoleDoUsuario() {
        Usuario usuario = Usuario.builder().id(1L).role(UsuarioRole.COMUM).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.updateRole(1L, "PREMIUM");

        assertEquals(UsuarioRole.PREMIUM, resultado.getRole());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve atualizar usuario sem alterar o campo role")
    void deveAtualizarUsuarioSemAlterarRole() {
        var enderecoReq = new EnderecoRequest("01310100", "100");
        var request = new UsuarioRequest("Novo Nome", "novo@fiap.com.br", "novaSenha", "11", "912345678", enderecoReq);

        Endereco endereco = Endereco.builder().id(1L).cep("01310100").numero("100").build();
        Telefone telefone = Telefone.builder().id(1L).ddd("11").numero("912345678").build();
        Usuario usuarioExistente = Usuario.builder().id(1L).nome("Enzo").role(UsuarioRole.ADMIN).telefone(telefone).endereco(endereco).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.encode("novaSenha")).thenReturn("hashedNova");
        when(enderecoService.findOrCreateByCepAndNumero(enderecoReq)).thenReturn(endereco);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.update(1L, request);

        assertEquals(UsuarioRole.ADMIN, resultado.getRole());
        assertEquals("Novo Nome", resultado.getNome());
    }

    @Test
    @DisplayName("Deve excluir usuario e seus pets onde eh titular, desvinculando co-cuidadores")
    void deveExcluirUsuarioComPetsOndeEhTitular() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        UsuarioPet vinculoTitular = UsuarioPet.builder()
                .id(new UsuarioPetId(1L, 10L))
                .pet(pet)
                .responsavelPrincipal(true)
                .build();
        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("enzo@fiap.com.br")
                .usuarioPets(Set.of(vinculoTitular))
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.delete(1L);

        verify(petService).delete(10L);
        verify(usuarioRepository).delete(usuario);
    }

    @Test
    @DisplayName("Deve desvincular co-cuidador sem excluir o pet onde outro usuario eh titular")
    void deveDesvincularCoCuidadorSemExcluirPet() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        UsuarioPet vinculoCoCuidador = UsuarioPet.builder()
                .id(new UsuarioPetId(2L, 10L))
                .pet(pet)
                .responsavelPrincipal(false)
                .build();
        Usuario coCuidador = Usuario.builder()
                .id(2L)
                .email("luna@fiap.com.br")
                .usuarioPets(Set.of(vinculoCoCuidador))
                .build();

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(coCuidador));

        usuarioService.delete(2L);

        verify(petService, never()).delete(anyLong());
        verify(usuarioRepository).delete(coCuidador);
    }

    @Test
    @DisplayName("delete por email deve lancar IllegalArgumentException se for o unico ADMIN")
    void deletePorEmailDeveLancarExcecaoSeForUnicoAdmin() {
        Usuario admin = Usuario.builder()
                .id(1L)
                .email("admin@fiap.com.br")
                .role(UsuarioRole.ADMIN)
                .usuarioPets(Set.of())
                .build();

        when(usuarioRepository.findByEmail("admin@fiap.com.br")).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRole(UsuarioRole.ADMIN)).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.delete("admin@fiap.com.br"));

        assertEquals("Não é possível excluir o único administrador do sistema.", ex.getMessage());
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    @Test
    @DisplayName("delete por email deve permitir autoexclusao de ADMIN se houver outro ADMIN no sistema")
    void deletePorEmailDevePermitirExclusaoDeAdminSeHouverOutro() {
        Usuario admin = Usuario.builder()
                .id(1L)
                .email("admin@fiap.com.br")
                .role(UsuarioRole.ADMIN)
                .usuarioPets(Set.of())
                .build();

        when(usuarioRepository.findByEmail("admin@fiap.com.br")).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRole(UsuarioRole.ADMIN)).thenReturn(2L);

        usuarioService.delete("admin@fiap.com.br");

        verify(usuarioRepository).delete(admin);
    }

    @Test
    @DisplayName("delete por ID deve lancar IllegalArgumentException se for o unico ADMIN")
    void deletePorIdDeveLancarExcecaoSeForUnicoAdmin() {
        Usuario admin = Usuario.builder().id(1L).email("admin@fiap.com.br").role(UsuarioRole.ADMIN).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRole(UsuarioRole.ADMIN)).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> usuarioService.delete(1L));

        assertEquals("Não é possível excluir o único administrador do sistema.", ex.getMessage());
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    @Test
    @DisplayName("delete por ID deve permitir exclusao de ADMIN se houver outro ADMIN no sistema")
    void deletePorIdDevePermitirExclusaoDeAdminSeHouverOutro() {
        Usuario admin = Usuario.builder().id(1L).email("admin@fiap.com.br").role(UsuarioRole.ADMIN).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRole(UsuarioRole.ADMIN)).thenReturn(2L);

        usuarioService.delete(1L);

        verify(usuarioRepository).delete(admin);
    }

    @Test
    @DisplayName("updateRole deve impedir o rebaixamento do unico ADMIN")
    void updateRoleDeveImpedirRebaixarUnicoAdmin() {
        Usuario admin = Usuario.builder().id(1L).role(UsuarioRole.ADMIN).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRole(UsuarioRole.ADMIN)).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.updateRole(1L, "COMUM"));

        assertEquals("Não é possível rebaixar o único administrador do sistema.", ex.getMessage());
        assertEquals(UsuarioRole.ADMIN, admin.getRole());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("updateRole deve permitir rebaixamento de ADMIN se houver outro ADMIN no sistema")
    void updateRoleDevePermitirRebaixarAdminSeHouverOutro() {
        Usuario admin = Usuario.builder().id(1L).role(UsuarioRole.ADMIN).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRole(UsuarioRole.ADMIN)).thenReturn(2L);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.updateRole(1L, "COMUM");

        assertEquals(UsuarioRole.COMUM, resultado.getRole());
        verify(usuarioRepository).save(admin);
    }

    @Test
    @DisplayName("delete por email deve permitir exclusao se usuario for COMUM")
    void deletePorEmailDevePermitirExclusaoParaComum() {
        Usuario comum = Usuario.builder()
                .id(2L)
                .email("comum@fiap.com.br")
                .role(UsuarioRole.COMUM)
                .usuarioPets(Set.of())
                .build();

        when(usuarioRepository.findByEmail("comum@fiap.com.br")).thenReturn(Optional.of(comum));

        usuarioService.delete("comum@fiap.com.br");

        verify(usuarioRepository).delete(comum);
    }

    @Test
    @DisplayName("Deve atualizar usuario por email com sucesso")
    void deveAtualizarUsuarioPorEmail() {
        var enderecoReq = new EnderecoRequest("01310100", "100");
        var request = new UsuarioRequest("Enzo Atualizado", "enzo@fiap.com.br", "novaSenha123", "11", "999998888", enderecoReq);

        Endereco endereco = Endereco.builder().id(1L).cep("01310100").numero("100").build();
        Telefone telefone = Telefone.builder().id(1L).ddd("11").numero("999998888").build();
        Usuario usuarioExistente = Usuario.builder().id(1L).email("enzo@fiap.com.br").nome("Enzo Antigo").senha("hash").role(UsuarioRole.COMUM).telefone(telefone).endereco(endereco).build();

        when(usuarioRepository.findByEmail("enzo@fiap.com.br")).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novaHash");
        when(enderecoService.findOrCreateByCepAndNumero(enderecoReq)).thenReturn(endereco);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.update("enzo@fiap.com.br", request);

        assertNotNull(resultado);
        assertEquals("Enzo Atualizado", resultado.getNome());
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao buscar usuario por email inexistente")
    void deveLancarExceptionAoBuscarUsuarioPorEmailInexistente() {
        when(usuarioRepository.findByEmail("naoexiste@fiap.com.br")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> usuarioService.findByEmail("naoexiste@fiap.com.br"));
    }

}
