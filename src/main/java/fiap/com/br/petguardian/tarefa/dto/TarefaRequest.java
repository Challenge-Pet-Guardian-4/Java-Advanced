package fiap.com.br.petguardian.tarefa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.tarefa.Tarefa;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.validation.EnumValidation;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TarefaRequest(
        @NotBlank
        String titulo,

        @NotNull
        @Positive(message = "Pontos da tarefa devem ser maiores que zero.")
        Integer pontosTarefa,

        @NotBlank
        String descricao,

        @NotNull(message = "O prazo da tarefa é obrigatório.")
        @Future(message = "O prazo da tarefa deve ser uma data e hora futura.")
        LocalDateTime prazo,

        @NotNull
        Long usuarioId,

        @NotNull
        Long petId,

        @NotBlank 
        @EnumValidation(enumClass = EnumStatus.class) 
        String status,

        LocalDateTime conclusao
) {
    public Tarefa toEntity(Usuario usuario, Pet pet, LocalDateTime criacao) {
        return Tarefa.builder()
                .titulo(titulo)
                .pontosTarefa(pontosTarefa)
                .descricao(descricao)
                .criacao(criacao)
                .prazo(prazo)
                .conclusao(conclusao)
                .usuario(usuario)
                .pet(pet)
                .build();
    }
}
