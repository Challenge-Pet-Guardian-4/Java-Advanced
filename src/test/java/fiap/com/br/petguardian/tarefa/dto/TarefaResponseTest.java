package fiap.com.br.petguardian.tarefa.dto;

import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.tarefa.Tarefa;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.tarefa.status.Status;
import fiap.com.br.petguardian.usuario.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TarefaResponseTest {

    @Test
    @DisplayName("Deve refletir fielmente o status da entidade na resposta DTO")
    void deveRefletirStatusDaEntidadeNoDto() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").nome("Enzo").build();
        Status statusPendente = Status.builder().id(1L).nomeStatus(EnumStatus.PENDENTE).build();

        Tarefa tarefa = Tarefa.builder()
                .id(1L)
                .titulo("Passeio")
                .pontosTarefa(20)
                .descricao("Passear com o pet")
                .criacao(LocalDateTime.now().minusDays(2))
                .prazo(LocalDateTime.now().minusHours(1))
                .status(statusPendente)
                .usuario(usuario)
                .pet(pet)
                .build();

        TarefaResponse response = TarefaResponse.fromEntity(tarefa);

        assertEquals("PENDENTE", response.status());
    }

    @Test
    @DisplayName("Deve manter status PENDENTE para tarefa pendente com prazo no futuro")
    void deveManterPendenteParaTarefaNoPrazo() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").nome("Enzo").build();
        Status statusPendente = Status.builder().id(1L).nomeStatus(EnumStatus.PENDENTE).build();

        Tarefa tarefa = Tarefa.builder()
                .id(2L)
                .titulo("Dar racao")
                .pontosTarefa(10)
                .descricao("Alimentar o pet")
                .criacao(LocalDateTime.now().minusHours(1))
                .prazo(LocalDateTime.now().plusHours(3))
                .status(statusPendente)
                .usuario(usuario)
                .pet(pet)
                .build();

        TarefaResponse response = TarefaResponse.fromEntity(tarefa);

        assertEquals("PENDENTE", response.status());
    }

    @Test
    @DisplayName("Deve manter status CONCLUIDO mesmo se o prazo estiver no passado")
    void deveManterConcluidoMesmoComPrazoPassado() {
        Pet pet = Pet.builder().id(10L).build();
        Usuario usuario = Usuario.builder().id(1L).email("enzo@fiap.com.br").nome("Enzo").build();
        Status statusConcluido = Status.builder().id(2L).nomeStatus(EnumStatus.CONCLUIDO).build();

        Tarefa tarefa = Tarefa.builder()
                .id(3L)
                .titulo("Dar banho")
                .pontosTarefa(30)
                .descricao("Banho no pet")
                .criacao(LocalDateTime.now().minusDays(3))
                .prazo(LocalDateTime.now().minusDays(1))
                .conclusao(LocalDateTime.now().minusDays(1))
                .status(statusConcluido)
                .usuario(usuario)
                .pet(pet)
                .build();

        TarefaResponse response = TarefaResponse.fromEntity(tarefa);

        assertEquals("CONCLUIDO", response.status());
    }
}
