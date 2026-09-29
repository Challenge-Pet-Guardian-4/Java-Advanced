package fiap.com.br.petguardian.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = NomeUnicoValidator.class)
public @interface NomeUnicoValidation {

    String message() default "Ja existe um registro cadastrado com este nome neste escopo.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
