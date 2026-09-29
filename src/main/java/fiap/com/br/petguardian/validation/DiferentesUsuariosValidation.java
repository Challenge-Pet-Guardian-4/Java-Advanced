package fiap.com.br.petguardian.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = DiferentesUsuariosValidator.class)
public @interface DiferentesUsuariosValidation {

    String message() default "O novo responsavel nao pode ser o mesmo que o responsavel atual.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
