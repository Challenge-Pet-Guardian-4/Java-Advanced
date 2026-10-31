package fiap.com.br.petguardian.tarefa;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetService;
import fiap.com.br.petguardian.tarefa.dto.TarefaRequest;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.tarefa.status.Status;
import fiap.com.br.petguardian.tarefa.status.StatusService;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioService;
import fiap.com.br.petguardian.usuariopet.UsuarioPetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TarefaService {
    private final TarefaRepository tarefaRepository;
    private final UsuarioService usuarioService;
    private final PetService petService;
    private final StatusService statusService;
    private final UsuarioPetRepository usuarioPetRepository;

    public Page<Tarefa> findAll(Pageable pageable) {
        expirarTarefasPendentesAtrasadas();
        return tarefaRepository.findAll(pageable);
    }

    public Page<Tarefa> findAllByUsuario(Long usuarioId, String statusFiltro, Pageable pageable) {
        expirarTarefasPendentesAtrasadas();
        if ("ALL".equalsIgnoreCase(statusFiltro)) {
            return tarefaRepository.findAllByPetUsuarioPetsUsuarioId(usuarioId, pageable);
        }
        return tarefaRepository.findAllByPetUsuarioPetsUsuarioIdAndStatusNomeStatus(usuarioId, EnumStatus.valueOf(statusFiltro.trim().toUpperCase()), pageable);
    }

    public Page<Tarefa> findAllByEmail(String email, String statusFiltro, Pageable pageable) {
        Usuario usuario = usuarioService.findByEmail(email);
        return findAllByUsuario(usuario.getId(), statusFiltro, pageable);
    }

    public Page<Tarefa> findAllByPetId(Long petId, Pageable pageable) {
        expirarTarefasPendentesAtrasadas();
        petService.findById(petId);
        return tarefaRepository.findAllByPetId(petId, pageable);
    }

    public Tarefa findById(Long id) {
        expirarTarefasPendentesAtrasadas();
        return findTarefaById(id);
    }

    @Transactional
    public Tarefa create(TarefaRequest request, String authEmail) {
        Pet pet = petService.findById(request.petId());
        Usuario usuario = usuarioService.findByEmail(authEmail);

        Tarefa tarefa = request.toEntity(usuario, pet, LocalDateTime.now());
        tarefa.setStatus(statusService.findStatus(EnumStatus.PENDENTE));
        tarefa.setConclusao(null);
        return tarefaRepository.save(tarefa);
    }

    @Transactional
    public Tarefa update(Long id, TarefaRequest request) {
        Tarefa tarefa = findTarefaById(id);
        Pet pet = petService.findById(request.petId());
        EnumStatus status = EnumStatus.valueOf(request.status().trim().toUpperCase());

        aplicarEm(tarefa, request, pet, status);
        return tarefaRepository.save(tarefa);
    }

    @Transactional
    public void delete(Long id) {
        findTarefaById(id);
        tarefaRepository.deleteById(id);
    }

    @Transactional
    public Tarefa concluir(Long id) {
        expirarTarefasPendentesAtrasadas();
        Tarefa tarefa = findTarefaById(id);
        validarPendenteParaConclusao(tarefa);

        tarefa.setStatus(statusService.findStatus(EnumStatus.CONCLUIDO));
        tarefa.setConclusao(LocalDateTime.now());
        return tarefaRepository.save(tarefa);
    }

    @Transactional
    public Tarefa desmarcar(Long id) {
        Tarefa tarefa = findTarefaById(id);
        validarConcluidaParaDesmarcar(tarefa);

        tarefa.setStatus(statusService.findStatus(EnumStatus.PENDENTE));
        tarefa.setConclusao(null);
        return tarefaRepository.save(tarefa);
    }

    @Transactional(readOnly = true)
    public boolean isCuidadorDaTarefa(Long tarefaId, String authEmail) {
        Tarefa tarefa = findTarefaById(tarefaId);
        return usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId(authEmail, tarefa.getPet().getId());
    }

    private Tarefa findTarefaById(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa com id " + id + " não encontrada."));
    }


    private void expirarTarefasPendentesAtrasadas() {
        Status pendente = statusService.findStatus(EnumStatus.PENDENTE);
        Status expirado = statusService.findStatus(EnumStatus.EXPIRADO);
        tarefaRepository.expirarTarefasPendentesAtrasadas(LocalDateTime.now(), pendente, expirado);
    }

    private void aplicarEm(Tarefa tarefa, TarefaRequest request, Pet pet, EnumStatus statusEnum) {
        tarefa.setTitulo(request.titulo());
        tarefa.setPontosTarefa(request.pontosTarefa());
        tarefa.setDescricao(request.descricao());
        tarefa.setPrazo(request.prazo());
        tarefa.setPet(pet);
        tarefa.setStatus(statusService.findStatus(statusEnum));
        tarefa.setConclusao(statusEnum == EnumStatus.CONCLUIDO
                ? (request.conclusao() != null ? request.conclusao() : LocalDateTime.now())
                : null);
    }

    private void validarPendenteParaConclusao(Tarefa tarefa) {
        if (tarefa.getStatus().getNomeStatus() != EnumStatus.PENDENTE) {
            throw new IllegalArgumentException("Apenas tarefas pendentes podem ser concluídas.");
        }
    }

    private void validarConcluidaParaDesmarcar(Tarefa tarefa) {
        if (tarefa.getStatus().getNomeStatus() != EnumStatus.CONCLUIDO) {
            throw new IllegalArgumentException("Apenas tarefas concluídas podem ser desmarcadas.");
        }
    }
}
