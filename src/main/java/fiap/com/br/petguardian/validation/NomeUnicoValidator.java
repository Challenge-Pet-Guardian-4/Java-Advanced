package fiap.com.br.petguardian.validation;

import fiap.com.br.petguardian.trilha.TrilhaRepository;
import fiap.com.br.petguardian.trilha.aula.AulaRepository;
import fiap.com.br.petguardian.trilha.aula.dto.AulaRequest;
import fiap.com.br.petguardian.trilha.dto.TrilhaRequest;
import fiap.com.br.petguardian.trilha.modulo.ModuloRepository;
import fiap.com.br.petguardian.trilha.modulo.dto.ModuloRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NomeUnicoValidator implements ConstraintValidator<NomeUnicoValidation, Object> {

    private final TrilhaRepository trilhaRepository;
    private final ModuloRepository moduloRepository;
    private final AulaRepository aulaRepository;

    @Autowired(required = false)
    private HttpServletRequest httpRequest;

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        Long currentId = extractId();
        boolean exists = false;

        if (value instanceof TrilhaRequest r) {
            exists = r.nome() != null && r.petId() != null && (currentId != null
                    ? trilhaRepository.existsByNomeIgnoreCaseAndPetIdAndIdNot(r.nome().trim(), r.petId(), currentId)
                    : trilhaRepository.existsByNomeIgnoreCaseAndPetId(r.nome().trim(), r.petId()));
        } else if (value instanceof ModuloRequest r) {
            exists = r.nome() != null && r.trilhaId() != null && (currentId != null
                    ? moduloRepository.existsByNomeIgnoreCaseAndTrilhaIdAndIdNot(r.nome().trim(), r.trilhaId(), currentId)
                    : moduloRepository.existsByNomeIgnoreCaseAndTrilhaId(r.nome().trim(), r.trilhaId()));
        } else if (value instanceof AulaRequest r) {
            exists = r.nome() != null && r.moduloId() != null && (currentId != null
                    ? aulaRepository.existsByNomeIgnoreCaseAndModuloIdAndIdNot(r.nome().trim(), r.moduloId(), currentId)
                    : aulaRepository.existsByNomeIgnoreCaseAndModuloId(r.nome().trim(), r.moduloId()));
        }

        if (exists) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("nome")
                        .addConstraintViolation();
            }
            return false;
        }

        return true;
    }

    private Long extractId() {
        if (httpRequest == null) {
            return null;
        }
        Object pathVariables = httpRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables instanceof Map<?, ?> vars && vars.containsKey("id")) {
            try {
                return Long.valueOf(vars.get("id").toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
