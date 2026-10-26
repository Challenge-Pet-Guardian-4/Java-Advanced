package fiap.com.br.petguardian.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Constraint(validatedBy = DataNascValidator.class)
public @interface DataNascValidation {
    int maxAnos() default 30;

    String message() default "A data de nascimento do pet não pode ultrapassar 30 anos.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
