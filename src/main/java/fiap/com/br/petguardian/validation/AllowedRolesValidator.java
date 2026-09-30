package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.usuario.UsuarioRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AllowedRolesValidator implements ConstraintValidator<AllowedRolesValidation, String> {

    private Set<String> allowedValues;

    @Override
    public void initialize(AllowedRolesValidation annotation) {
        allowedValues = Stream.of(annotation.allowed())
                .map(UsuarioRole::name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return allowedValues.contains(value.trim().toUpperCase());
    }
}
