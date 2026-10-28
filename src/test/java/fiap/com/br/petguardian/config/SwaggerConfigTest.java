package fiap.com.br.petguardian.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {

    @Test
    @DisplayName("Deve registrar os redirecionamentos de URL no ViewControllerRegistry")
    void deveRegistrarViewControllers() {
        SwaggerConfig config = new SwaggerConfig();
        org.springframework.context.ApplicationContext context = org.mockito.Mockito.mock(org.springframework.context.ApplicationContext.class);
        ViewControllerRegistry registry = new ViewControllerRegistry(context);
        assertDoesNotThrow(() -> config.addViewControllers(registry));
    }

    @Test
    @DisplayName("Deve ordenar as tags conforme ordem definida no customizer")
    void deveOrdenarTagsConformeOrdem() {
        SwaggerConfig config = new SwaggerConfig();
        var customizer = config.sortTagsCustomizer();

        OpenAPI openApi = new OpenAPI();
        List<Tag> tags = new ArrayList<>(List.of(
                new Tag().name("Endereco"),
                new Tag().name("Usuário"),
                new Tag().name("TagInexistenteNaLista"),
                new Tag().name("Autenticação")
        ));
        openApi.setTags(tags);

        customizer.customise(openApi);

        assertEquals("Usuário", openApi.getTags().get(0).getName());
        assertEquals("Autenticação", openApi.getTags().get(1).getName());
        assertEquals("Endereco", openApi.getTags().get(2).getName());
        assertEquals("TagInexistenteNaLista", openApi.getTags().get(3).getName());
    }

    @Test
    @DisplayName("Customizer deve suportar lista de tags nula com seguranca")
    void deveTratarListaDeTagsNula() {
        SwaggerConfig config = new SwaggerConfig();
        var customizer = config.sortTagsCustomizer();

        OpenAPI openApi = new OpenAPI();
        openApi.setTags(null);

        assertDoesNotThrow(() -> customizer.customise(openApi));
    }
}
