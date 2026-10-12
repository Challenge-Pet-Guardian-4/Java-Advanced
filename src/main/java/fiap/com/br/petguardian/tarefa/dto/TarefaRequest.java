package fiap.com.br.petguardian.tarefa.dto;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.tarefa.Tarefa;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.validation.EnumValidation;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TarefaRequest(
        @NotBlank
        String titulo,

        @NotNull
        @Min(value = 0, message = "Pontos da tarefa não podem ser negativos.")
        @Max(value = 10, message = "Pontos da tarefa devem ser no máximo 10.")
        Integer pontosTarefa,

        @NotBlank
        String descricao,

        @NotNull(message = "O prazo da tarefa é obrigatório.")
        @Future(message = "O prazo da tarefa deve ser uma data e hora futura.")
        LocalDateTime prazo,

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
