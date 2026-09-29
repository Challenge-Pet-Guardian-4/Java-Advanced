package fiap.com.br.petguardian.trilha;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetRepository;
import fiap.com.br.petguardian.trilha.dto.TrilhaRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrilhaServiceTest {

    @Mock
    private TrilhaRepository trilhaRepository;

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private TrilhaService trilhaService;

    @Test
    @DisplayName("Deve criar trilha associada a um pet")
    void deveCriarTrilha() {
        Pet pet = Pet.builder().id(10L).nome("Thor").build();
        var request = new TrilhaRequest("Obediência Básica", "Primeiros comandos", 10L);
        Trilha trilhaSalva = Trilha.builder().id(1L).nome("Obediência Básica").descricao("Primeiros comandos").pet(pet).build();

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(trilhaRepository.save(any(Trilha.class))).thenReturn(trilhaSalva);

        Trilha resultado = trilhaService.create(request);

        assertNotNull(resultado);
        assertEquals("Obediência Básica", resultado.getNome());
        verify(trilhaRepository).save(any(Trilha.class));
    }

    @Test
    @DisplayName("Deve listar trilhas por pet")
    void deveListarTrilhasPorPet() {
        Pet pet = Pet.builder().id(10L).build();
        Trilha trilha = Trilha.builder().id(1L).nome("Truques").pet(pet).build();

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(trilhaRepository.findAllByPetId(10L)).thenReturn(List.of(trilha));

        List<Trilha> resultado = trilhaService.findAllByPetId(10L);

        assertEquals(1, resultado.size());
        assertEquals("Truques", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Deve deletar trilha")
    void deveDeletarTrilha() {
        Trilha trilha = Trilha.builder().id(1L).build();
        when(trilhaRepository.findById(1L)).thenReturn(Optional.of(trilha));

        trilhaService.delete(1L);

        verify(trilhaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve atualizar trilha preservando modulos existentes")
    void deveAtualizarTrilhaPreservandoModulos() {
        Pet pet = Pet.builder().id(10L).build();
        Trilha trilhaExistente = Trilha.builder().id(1L).nome("Trilha Antiga").descricao("Desc").pet(pet).build();
        var request = new TrilhaRequest("Trilha Nova", "Nova Desc", 10L);

        when(trilhaRepository.findById(1L)).thenReturn(Optional.of(trilhaExistente));
        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(trilhaRepository.save(any(Trilha.class))).thenAnswer(inv -> inv.getArgument(0));

        Trilha resultado = trilhaService.update(1L, request);

        assertNotNull(resultado);
        assertEquals("Trilha Nova", resultado.getNome());
        assertEquals(trilhaExistente.getModulos(), resultado.getModulos());
    }
}
