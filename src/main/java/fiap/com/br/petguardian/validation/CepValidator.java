package fiap.com.br.petguardian.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class CepValidator implements ConstraintValidator<CepValidation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        // Valida se o CEP tem 8 dígitos (ex: 01311000) ou formato com hífen (ex: 01311-000)
        return value.matches("^\\d{8}$") || value.matches("^\\d{5}-\\d{3}$");
    }
}
