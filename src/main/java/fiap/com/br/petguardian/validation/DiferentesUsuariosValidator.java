package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.usuariopet.dto.TransferirResponsabilidadeRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DiferentesUsuariosValidator implements ConstraintValidator<DiferentesUsuariosValidation, TransferirResponsabilidadeRequest> {

    @Override
    public boolean isValid(TransferirResponsabilidadeRequest value, ConstraintValidatorContext context) {
        return value == null || !Objects.equals(value.responsavelAtualId(), value.novoResponsavelId());
    }
}
