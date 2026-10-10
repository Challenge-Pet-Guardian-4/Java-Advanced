package fiap.com.br.petguardian.tarefa;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.pet.Pet;
import fiap.com.br.petguardian.pet.PetRepository;
import fiap.com.br.petguardian.tarefa.status.EnumStatus;
import fiap.com.br.petguardian.tarefa.status.Status;
import fiap.com.br.petguardian.tarefa.status.StatusService;
import fiap.com.br.petguardian.tarefa.dto.TarefaRequest;
import fiap.com.br.petguardian.usuario.Usuario;
import fiap.com.br.petguardian.usuario.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;
    private final PetRepository petRepository;
    private final StatusService statusService;
    private final UsuarioPetRepository usuarioPetRepository;

    public boolean isCuidadorDaTarefa(Long tarefaId, String email) {
        Tarefa tarefa = findTarefaById(tarefaId);
        return usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId(email.trim(), tarefa.getPet().getId());
    }

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
        Usuario usuario = findUsuarioByEmail(email);
        return findAllByUsuario(usuario.getId(), statusFiltro, pageable);
    }

    @Transactional(readOnly = true)
    public Integer calcularPontosTotaisEmail(String email) {
        Usuario usuario = findUsuarioByEmail(email);
        return calcularPontosTotaisUsuario(usuario.getId());
    }

    public Page<Tarefa> findAllByPetId(Long petId, Pageable pageable) {
        expirarTarefasPendentesAtrasadas();
        findPetById(petId);
        return tarefaRepository.findAllByPetId(petId, pageable);
    }

    public Tarefa findById(Long id) {
        expirarTarefasPendentesAtrasadas();
        return findTarefaById(id);
    }

    @Transactional
    public Tarefa create(TarefaRequest request, String authEmail) {
        Pet pet = findPetById(request.petId());
        validarCuidadorDoPet(authEmail, pet.getId());
        Usuario usuario = findUsuarioByEmail(authEmail);

        Tarefa tarefa = request.toEntity(usuario, pet, LocalDateTime.now());
        tarefa.setStatus(statusService.findStatus(EnumStatus.PENDENTE));
        return tarefaRepository.save(tarefa);
    }

    @Transactional
    public Tarefa update(Long id, TarefaRequest request, String authEmail) {
        Tarefa tarefa = findTarefaById(id);
        Pet pet = findPetById(request.petId());
        validarCuidadorDoPet(authEmail, pet.getId());
        Usuario usuario = findUsuarioByEmail(authEmail);
        EnumStatus status = EnumStatus.valueOf(request.status().trim().toUpperCase());

        LocalDateTime conclusao = definirConclusao(tarefa, status, request.conclusao(), LocalDateTime.now());
        aplicarEm(tarefa, request, usuario, pet, statusService.findStatus(status), conclusao);
        return tarefaRepository.save(tarefa);
    }

    @Transactional
    public void delete(Long id) {
        findTarefaById(id);
        tarefaRepository.deleteById(id);
    }

    @Transactional
    public Tarefa concluir(Long id, String authEmail) {
        Tarefa tarefa = findTarefaById(id);
        validarPendenteParaConclusao(tarefa);
        validarCuidadorDoPet(authEmail, tarefa.getPet().getId());
        Usuario usuario = findUsuarioByEmail(authEmail);

        aplicarConclusao(tarefa, usuario, statusService.findStatus(EnumStatus.CONCLUIDO));
        return tarefaRepository.save(tarefa);
    }

    @Transactional
    public Tarefa desmarcar(Long id, String authEmail) {
        Tarefa tarefa = findTarefaById(id);
        validarCuidadorDoPet(authEmail, tarefa.getPet().getId());
        validarConcluidaParaDesmarcar(tarefa);

        tarefa.setStatus(statusService.findStatus(EnumStatus.PENDENTE));
        tarefa.setConclusao(null);
        return tarefaRepository.save(tarefa);
    }

    @Transactional(readOnly = true)
    public Integer calcularPontosTotaisUsuario(Long usuarioId) {
        findUsuarioById(usuarioId);
        return tarefaRepository.calcularPontosTotaisUsuario(usuarioId, EnumStatus.CONCLUIDO);
    }

    private Tarefa findTarefaById(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa com id " + id + " nao encontrada."));
    }

    private LocalDateTime definirConclusao(Tarefa tarefa, EnumStatus status, LocalDateTime conclusaoInformada, LocalDateTime agora) {
        if (status != EnumStatus.CONCLUIDO) return null;
        return conclusaoInformada != null ? conclusaoInformada : (tarefa.getConclusao() != null ? tarefa.getConclusao() : agora);
    }

    private Pet findPetById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet com id " + id + " nao encontrado."));
    }

    private Usuario findUsuarioById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com id " + id + " nao encontrado."));
    }

    private Usuario findUsuarioByEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com email " + email + " nao encontrado."));
    }

    private void expirarTarefasPendentesAtrasadas() {
        Status pendente = statusService.findStatus(EnumStatus.PENDENTE);
        Status expirado = statusService.findStatus(EnumStatus.EXPIRADO);
        tarefaRepository.expirarTarefasPendentesAtrasadas(LocalDateTime.now(), pendente, expirado);
    }

    private void aplicarEm(Tarefa tarefa, TarefaRequest request, Usuario usuario, Pet pet, Status statusObj, LocalDateTime conclusaoCalculada) {
        tarefa.setTitulo(request.titulo());
        tarefa.setPontosTarefa(request.pontosTarefa());
        tarefa.setDescricao(request.descricao());
        tarefa.setPrazo(request.prazo());
        tarefa.setUsuario(usuario);
        tarefa.setPet(pet);
        tarefa.setStatus(statusObj);
        tarefa.setConclusao(conclusaoCalculada);
    }

    private void aplicarConclusao(Tarefa tarefa, Usuario usuario, Status statusConcluido) {
        tarefa.setUsuario(usuario);
        tarefa.setStatus(statusConcluido);
        tarefa.setConclusao(LocalDateTime.now());
    }

    private void validarCuidadorDoPet(String email, Long petId) {
        if (!usuarioPetRepository.existsByUsuarioEmailIgnoreCaseAndPetId(email.trim(), petId)) {
            throw new IllegalArgumentException("Usuario informado nao esta vinculado ao pet da tarefa.");
        }
    }

    private void validarPendenteParaConclusao(Tarefa tarefa) {
        if (tarefa.getStatus().getNomeStatus() != EnumStatus.PENDENTE) {
            throw new IllegalArgumentException("Apenas tarefas pendentes podem ser concluidas.");
        }
    }

    private void validarConcluidaParaDesmarcar(Tarefa tarefa) {
        if (tarefa.getStatus().getNomeStatus() != EnumStatus.CONCLUIDO) {
            throw new IllegalArgumentException("Apenas tarefas concluidas podem ser desmarcadas.");
        }
    }
}
