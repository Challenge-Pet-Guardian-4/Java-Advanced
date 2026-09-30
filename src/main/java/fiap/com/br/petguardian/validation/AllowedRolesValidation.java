package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.usuario.UsuarioRole;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Constraint(validatedBy = AllowedRolesValidator.class)
public @interface AllowedRolesValidation {

    UsuarioRole[] allowed();

    String message() default "Role não permitido para este endpoint.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
