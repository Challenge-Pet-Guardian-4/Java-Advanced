package fiap.com.br.petguardian.trilha.aula;

import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAulaRepository;
import fiap.com.br.petguardian.trilha.aula.dto.AulaRequest;
import fiap.com.br.petguardian.trilha.aula.dto.AulaResponse;
import fiap.com.br.petguardian.trilha.modulo.Modulo;
import fiap.com.br.petguardian.trilha.modulo.ModuloRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
import fiap.com.br.petguardian.usuario.UsuarioRole;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.trilha.Trilha;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AulaServiceTest {

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private ModuloRepository moduloRepository;

    @Mock
    private UsuarioPetRepository usuarioPetRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ConteudoAulaRepository conteudoRepository;

    @InjectMocks
    private AulaService aulaService;

    @Test
    @DisplayName("Deve criar aula associada a um modulo")
    void deveCriarAula() {
        Modulo modulo = Modulo.builder().id(10L).nome("Módulo 1").build();
        var request = new AulaRequest("Comando Senta!", "Ensine o pet a sentar", 10, "FACIL", false, 10L);
        Aula aulaSalva = Aula.builder().id(100L).nome("Comando Senta!").descricao("Ensine o pet a sentar").pontosAula(10).dificuldade("FACIL").concluida(false).modulo(modulo).build();

        when(moduloRepository.findById(10L)).thenReturn(Optional.of(modulo));
        when(aulaRepository.save(any(Aula.class))).thenReturn(aulaSalva);

        AulaResponse resultado = aulaService.create(request);

        assertNotNull(resultado);
        assertEquals("Comando Senta!", resultado.nome());
        assertEquals(10, resultado.pontosAula());
        assertNull(resultado.conteudo());
        verify(aulaRepository).save(any(Aula.class));
    }

    @Test
    @DisplayName("Deve listar aulas por modulo")
    void deveListarAulasPorModulo() {
        Modulo modulo = Modulo.builder().id(10L).build();
        Aula aula = Aula.builder().id(100L).nome("Comando Senta!").modulo(modulo).build();

        when(moduloRepository.findById(10L)).thenReturn(Optional.of(modulo));
        when(aulaRepository.findAllByModuloId(10L)).thenReturn(List.of(aula));

        List<AulaResponse> resultado = aulaService.findAllByModuloId(10L);

        assertEquals(1, resultado.size());
        assertEquals("Comando Senta!", resultado.get(0).nome());
    }


    @Test
    @DisplayName("Deve deletar aula")
    void deveDeletarAula() {
        Aula aula = Aula.builder().id(100L).build();
        when(aulaRepository.findById(100L)).thenReturn(Optional.of(aula));

        aulaService.delete(100L);

        verify(aulaRepository).deleteById(100L);
        verify(conteudoRepository).deleteByAulaId(100L);
    }

    @Test
    @DisplayName("Deve concluir aula quando usuario autenticado for cuidador do pet da trilha")
    void deveConcluirAulaComCuidadorValido() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Trilha trilha = Trilha.builder().id(5L).pet(pet).build();
        Modulo modulo = Modulo.builder().id(20L).trilha(trilha).build();
        Aula aula = Aula.builder().id(100L).concluida(false).modulo(modulo).build();
        Usuario tutor = Usuario.builder().id(1L).email("carolina@petguardian.com").role(UsuarioRole.PREMIUM).build();

        when(aulaRepository.findById(100L)).thenReturn(Optional.of(aula));
        when(usuarioRepository.findByEmail("carolina@petguardian.com")).thenReturn(Optional.of(tutor));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("carolina@petguardian.com", 10L)).thenReturn(true);
        when(aulaRepository.save(any(Aula.class))).thenAnswer(inv -> inv.getArgument(0));

        AulaResponse resultado = aulaService.concluir(100L, "carolina@petguardian.com");

        assertTrue(resultado.concluida());
        verify(aulaRepository).save(aula);
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar concluir aula quando usuario nao for cuidador do pet da trilha")
    void deveLancarExcecaoAoConcluirAulaUsuarioNaoCuidador() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Trilha trilha = Trilha.builder().id(5L).pet(pet).build();
        Modulo modulo = Modulo.builder().id(20L).trilha(trilha).build();
        Aula aula = Aula.builder().id(100L).concluida(false).modulo(modulo).build();
        Usuario invasor = Usuario.builder().id(99L).email("invasor@petguardian.com").role(UsuarioRole.PREMIUM).build();

        when(aulaRepository.findById(100L)).thenReturn(Optional.of(aula));
        when(usuarioRepository.findByEmail("invasor@petguardian.com")).thenReturn(Optional.of(invasor));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("invasor@petguardian.com", 10L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> aulaService.concluir(100L, "invasor@petguardian.com"));
    }

    @Test
    @DisplayName("Deve desmarcar aula quando usuario autenticado for cuidador do pet da trilha")
    void deveDesmarcarAulaComCuidadorValido() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        Trilha trilha = Trilha.builder().id(5L).pet(pet).build();
        Modulo modulo = Modulo.builder().id(20L).trilha(trilha).build();
        Aula aula = Aula.builder().id(100L).concluida(true).modulo(modulo).build();
        Usuario tutor = Usuario.builder().id(1L).email("carolina@petguardian.com").role(UsuarioRole.PREMIUM).build();

        when(aulaRepository.findById(100L)).thenReturn(Optional.of(aula));
        when(usuarioRepository.findByEmail("carolina@petguardian.com")).thenReturn(Optional.of(tutor));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("carolina@petguardian.com", 10L)).thenReturn(true);
        when(aulaRepository.save(any(Aula.class))).thenAnswer(inv -> inv.getArgument(0));

        AulaResponse resultado = aulaService.desmarcar(100L, "carolina@petguardian.com");

        assertFalse(resultado.concluida());
        verify(aulaRepository).save(aula);
    }

    @Test
    @DisplayName("isCuidadorDaAula deve retornar true quando email pertence a cuidador do pet")
    void isCuidadorDaAulaDeveRetornarTrue() {
        Pet pet = Pet.builder().id(10L).build();
        Trilha trilha = Trilha.builder().id(5L).pet(pet).build();
        Modulo modulo = Modulo.builder().id(20L).trilha(trilha).build();
        Aula aula = Aula.builder().id(100L).modulo(modulo).build();

        when(aulaRepository.findById(100L)).thenReturn(Optional.of(aula));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("cuidador@petguardian.com", 10L)).thenReturn(true);

        assertTrue(aulaService.isCuidadorDaAula(100L, "cuidador@petguardian.com"));
    }

    @Test
    @DisplayName("isCuidadorDaAula deve retornar false quando email nao pertence a cuidador do pet")
    void isCuidadorDaAulaDeveRetornarFalse() {
        Pet pet = Pet.builder().id(10L).build();
        Trilha trilha = Trilha.builder().id(5L).pet(pet).build();
        Modulo modulo = Modulo.builder().id(20L).trilha(trilha).build();
        Aula aula = Aula.builder().id(100L).modulo(modulo).build();

        when(aulaRepository.findById(100L)).thenReturn(Optional.of(aula));
        when(usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId("outro@petguardian.com", 10L)).thenReturn(false);

        assertFalse(aulaService.isCuidadorDaAula(100L, "outro@petguardian.com"));
    }
}
