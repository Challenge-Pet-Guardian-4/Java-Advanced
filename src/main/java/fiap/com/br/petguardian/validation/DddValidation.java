package fiap.com.br.petguardian.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Constraint(validatedBy = DddValidator.class)
public @interface DddValidation {
    String message() default "DDD inválido! Deve ser um DDD válido do Brasil (2 dígitos).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
