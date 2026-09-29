package fiap.com.br.petguardian.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Constraint(validatedBy = EnumValidator.class)
public @interface EnumValidation {
    Class<? extends Enum<?>> enumClass();

    String message() default "Enum inserido incorretamente!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
