package fiap.com.br.petguardian.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(url = "/", description = "Default Server URL")
        },
        info = @Info(
                title = "PetGuardian API",
                description = "API da plataforma de cuidado colaborativo centrada no Pet. Tutores compartilham responsabilidades, gerenciam tarefas da rotina do animal e acompanham o histórico consolidado de cuidados em um único lugar.",
                version = "1.0"
        ),
        security = @SecurityRequirement(name = "bearerAuth"),
        tags = {
                @Tag(name = "Usuário", description = "Gerenciamento de usuários (tutores/cuidadores)"),
                @Tag(name = "Autenticação", description = "Autenticação de usuários e geração de token JWT"),
                @Tag(name = "Pets", description = "Gerenciamento de pets e histórico clínico"),
                @Tag(name = "UsuarioPet", description = "Gestão de vínculos entre usuários e pets"),
                @Tag(name = "Care Circle", description = "Gestão colaborativa de tutores e co-cuidadores do pet"),
                @Tag(name = "Tarefas", description = "Gerenciamento de tarefas e rotinas de cuidados do pet"),
                @Tag(name = "Historico", description = "Histórico de saúde e eventos do pet"),
                @Tag(name = "Trilhas", description = "Gerenciamento de trilhas de aprendizado e adestramento do pet"),
                @Tag(name = "Modulos", description = "Gerenciamento de módulos das trilhas de aprendizado"),
                @Tag(name = "Aulas", description = "Gerenciamento de aulas e conteúdos educativos das trilhas"),
                @Tag(name = "Endereco", description = "Gerenciamento de endereços")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Bean
    public OpenApiCustomizer sortTagsCustomizer() {
        return openApi -> {
            List<String> order = List.of(
                    "Usuário",
                    "Autenticação",
                    "Pets",
                    "UsuarioPet",
                    "Care Circle",
                    "Tarefas",
                    "Historico",
                    "Trilhas",
                    "Modulos",
                    "Aulas",
                    "Endereco"
            );
            if (openApi.getTags() != null) {
                openApi.getTags().sort(Comparator.comparingInt(tag -> {
                    int index = order.indexOf(tag.getName());
                    return index != -1 ? index : Integer.MAX_VALUE;
                }));
            }
        };
    }
}

