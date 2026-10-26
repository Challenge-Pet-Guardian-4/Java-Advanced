package fiap.com.br.petguardian.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataNascValidator implements ConstraintValidator<DataNascValidation, LocalDate> {

    private int maxAnos = 30;

    @Override
    public void initialize(DataNascValidation annotation) {
        this.maxAnos = annotation.maxAnos();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return !value.isBefore(LocalDate.now().minusYears(maxAnos));
    }
}
