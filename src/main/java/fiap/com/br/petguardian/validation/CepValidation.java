package fiap.com.br.petguardian.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Constraint(validatedBy = CepValidator.class)
public @interface CepValidation {
    String message() default "CEP inválido! Deve conter 8 dígitos numéricos (com ou sem hífen).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
