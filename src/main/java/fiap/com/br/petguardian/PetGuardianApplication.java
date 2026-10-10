package fiap.com.br.petguardian;

import fiap.com.br.petguardian.endereco.ViaCepService;
import fiap.com.br.petguardian.trilha.aula.conteudo.ConteudoAulaRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.service.registry.ImportHttpServices;

@SpringBootApplication
@EnableCaching
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@ConfigurationPropertiesScan
@ImportHttpServices(ViaCepService.class)
@EnableJpaRepositories(
        basePackageClasses = PetGuardianApplication.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ConteudoAulaRepository.class)
)
@EnableMongoRepositories(
        basePackageClasses = ConteudoAulaRepository.class
)
public class PetGuardianApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetGuardianApplication.class, args);
    }

}
