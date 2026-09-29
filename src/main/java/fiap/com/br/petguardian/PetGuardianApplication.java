package fiap.com.br.petguardian;

import fiap.com.br.petguardian.auth.SecurityConfig;
import fiap.com.br.petguardian.endereco.ViaCepService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.service.registry.ImportHttpServices;

@SpringBootApplication
@EnableCaching
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableConfigurationProperties(SecurityConfig.RsaKeyProperties.class)
@ConfigurationPropertiesScan
@ImportHttpServices(ViaCepService.class)
public class PetGuardianApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetGuardianApplication.class, args);
    }

}
