package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.trilha.TrilhaRepository;
import fiap.com.br.petguardian.trilha.aula.AulaRepository;
import fiap.com.br.petguardian.trilha.aula.dto.AulaRequest;
import fiap.com.br.petguardian.trilha.dto.TrilhaRequest;
import fiap.com.br.petguardian.trilha.modulo.ModuloRepository;
import fiap.com.br.petguardian.trilha.modulo.dto.ModuloRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NomeUnicoValidatorTest {

    @Mock
    private TrilhaRepository trilhaRepository;

    @Mock
    private ModuloRepository moduloRepository;

    @Mock
    private AulaRepository aulaRepository;

    private NomeUnicoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NomeUnicoValidator(trilhaRepository, moduloRepository, aulaRepository);
    }

    @Test
    @DisplayName("Deve validar trilha com nome inédito e rejeitar quando duplicado")
    void deveValidarTrilha() {
        var request = new TrilhaRequest("Obediência", "Desc", 1L);
        when(trilhaRepository.existsByNomeIgnoreCaseAndPetId("Obediência", 1L)).thenReturn(false);
        assertTrue(validator.isValid(request, null));

        when(trilhaRepository.existsByNomeIgnoreCaseAndPetId("Obediência", 1L)).thenReturn(true);
        assertFalse(validator.isValid(request, null));
    }

    @Test
    @DisplayName("Deve validar modulo com nome inédito e rejeitar quando duplicado")
    void deveValidarModulo() {
        var request = new ModuloRequest("Módulo 1", "10 min", "Desc", 1L);
        when(moduloRepository.existsByNomeIgnoreCaseAndTrilhaId("Módulo 1", 1L)).thenReturn(false);
        assertTrue(validator.isValid(request, null));

        when(moduloRepository.existsByNomeIgnoreCaseAndTrilhaId("Módulo 1", 1L)).thenReturn(true);
        assertFalse(validator.isValid(request, null));
    }

    @Test
    @DisplayName("Deve validar aula com nome inédito e rejeitar quando duplicado")
    void deveValidarAula() {
        var request = new AulaRequest("Aula 1", "Desc", 20, "FACIL", "Conteudo", false, 1L);
        when(aulaRepository.existsByNomeIgnoreCaseAndModuloId("Aula 1", 1L)).thenReturn(false);
        assertTrue(validator.isValid(request, null));

        when(aulaRepository.existsByNomeIgnoreCaseAndModuloId("Aula 1", 1L)).thenReturn(true);
        assertFalse(validator.isValid(request, null));
    }

    @Test
    @DisplayName("Deve aceitar se o request ou campos obrigatorios forem nulos (deixando para @NotNull/@NotBlank)")
    void deveAceitarNulos() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid(new TrilhaRequest(null, "Desc", 1L), null));
        assertTrue(validator.isValid(new TrilhaRequest("Nome", "Desc", null), null));
        assertTrue(validator.isValid(new ModuloRequest(null, "10 min", "Desc", 1L), null));
        assertTrue(validator.isValid(new ModuloRequest("Nome", "10 min", "Desc", null), null));
        assertTrue(validator.isValid(new AulaRequest(null, "Desc", 20, "FACIL", "Conteudo", false, 1L), null));
        assertTrue(validator.isValid(new AulaRequest("Aula 1", "Desc", 20, "FACIL", "Conteudo", false, null), null));
    }
}
