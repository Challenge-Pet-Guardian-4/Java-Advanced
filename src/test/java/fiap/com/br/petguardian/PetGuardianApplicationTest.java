package fiap.com.br.petguardian;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class PetGuardianApplicationTest {

    @Test
    @DisplayName("Deve inicializar a classe principal PetGuardianApplication")
    void deveInicializarMain() {
        try (MockedStatic<SpringApplication> mockedSpring = mockStatic(SpringApplication.class)) {
            mockedSpring.when(() -> SpringApplication.run(PetGuardianApplication.class, new String[]{}))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            assertDoesNotThrow(() -> PetGuardianApplication.main(new String[]{}));
            assertDoesNotThrow(PetGuardianApplication::new);
        }
    }
}
