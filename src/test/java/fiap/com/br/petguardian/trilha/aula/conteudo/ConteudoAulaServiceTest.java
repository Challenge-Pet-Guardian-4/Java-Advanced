package fiap.com.br.petguardian.trilha.aula.conteudo;

import fiap.com.br.petguardian.exception.ResourceNotFoundException;
import fiap.com.br.petguardian.trilha.aula.Aula;
import fiap.com.br.petguardian.trilha.aula.AulaService;
import fiap.com.br.petguardian.trilha.aula.conteudo.dto.ConteudoAulaRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConteudoAulaServiceTest {

    @Mock
    private ConteudoAulaRepository conteudoRepository;

    @Mock
    private AulaService aulaService;

    @InjectMocks
    private ConteudoAulaService conteudoService;

    @Test
    @DisplayName("Deve criar conteudo para aula com sucesso")
    void deveCriarConteudoComSucesso() {
        Long aulaId = 1L;
        Aula aula = Aula.builder().id(aulaId).nome("Aula de Comandos").build();
        var request = new ConteudoAulaRequest("MARKDOWN", "# Titulo da Aula", List.of("https://video.com/1"));

        ConteudoAula salvo = ConteudoAula.builder()
                .id("mongo-id-123")
                .aulaId(aulaId)
                .tipoConteudo("MARKDOWN")
                .corpoMarkdown("# Titulo da Aula")
                .linksRecursos(List.of("https://video.com/1"))
                .atualizadoEm(Instant.now())
                .build();

        when(aulaService.findAulaEntityById(aulaId)).thenReturn(aula);
        when(conteudoRepository.findByAulaId(aulaId)).thenReturn(Optional.empty());
        when(conteudoRepository.save(any(ConteudoAula.class))).thenReturn(salvo);

        ConteudoAula resultado = conteudoService.create(aulaId, request);

        assertNotNull(resultado);
        assertEquals("mongo-id-123", resultado.getId());
        assertEquals(aulaId, resultado.getAulaId());
        assertEquals("# Titulo da Aula", resultado.getCorpoMarkdown());
        verify(conteudoRepository).save(any(ConteudoAula.class));
    }

    @Test
    @DisplayName("Deve lancar IllegalArgumentException ao tentar criar conteudo para aula que ja possui conteudo")
    void deveLancarExcecaoAoCriarConteudoDuplicado() {
        Long aulaId = 1L;
        Aula aula = Aula.builder().id(aulaId).nome("Aula de Comandos").build();
        var request = new ConteudoAulaRequest("MARKDOWN", "# Titulo da Aula", List.of());
        ConteudoAula existente = ConteudoAula.builder().id("mongo-existente").aulaId(aulaId).build();

        when(aulaService.findAulaEntityById(aulaId)).thenReturn(aula);
        when(conteudoRepository.findByAulaId(aulaId)).thenReturn(Optional.of(existente));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> conteudoService.create(aulaId, request)
        );

        assertTrue(ex.getMessage().contains("Já existe conteúdo cadastrado"));
        verify(conteudoRepository, never()).save(any(ConteudoAula.class));
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao tentar criar conteudo para aula inexistente")
    void deveLancarExcecaoAoCriarConteudoParaAulaInexistente() {
        Long aulaId = 999L;
        var request = new ConteudoAulaRequest("MARKDOWN", "# Titulo", List.of());

        when(aulaService.findAulaEntityById(aulaId)).thenThrow(new ResourceNotFoundException("Aula não encontrada"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> conteudoService.create(aulaId, request)
        );

        verify(conteudoRepository, never()).save(any(ConteudoAula.class));
    }

    @Test
    @DisplayName("Deve buscar conteudo por aulaId com sucesso")
    void deveBuscarConteudoPorAulaIdComSucesso() {
        Long aulaId = 2L;
        Aula aula = Aula.builder().id(aulaId).build();
        ConteudoAula conteudo = ConteudoAula.builder()
                .id("mongo-id-456")
                .aulaId(aulaId)
                .corpoMarkdown("Conteudo da aula 2")
                .build();

        when(aulaService.findAulaEntityById(aulaId)).thenReturn(aula);
        when(conteudoRepository.findByAulaId(aulaId)).thenReturn(Optional.of(conteudo));

        ConteudoAula resultado = conteudoService.findByAulaId(aulaId);

        assertNotNull(resultado);
        assertEquals("mongo-id-456", resultado.getId());
        assertEquals(aulaId, resultado.getAulaId());
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao buscar conteudo para aula inexistente")
    void deveLancarExcecaoAoBuscarConteudoParaAulaInexistente() {
        Long aulaId = 999L;
        when(aulaService.findAulaEntityById(aulaId)).thenThrow(new ResourceNotFoundException("Aula não encontrada"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> conteudoService.findByAulaId(aulaId)
        );
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao buscar conteudo quando aula existe mas conteudo nao existe")
    void deveLancarExcecaoAoBuscarConteudoInexistenteParaAula() {
        Long aulaId = 3L;
        Aula aula = Aula.builder().id(aulaId).build();

        when(aulaService.findAulaEntityById(aulaId)).thenReturn(aula);
        when(conteudoRepository.findByAulaId(aulaId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> conteudoService.findByAulaId(aulaId)
        );
    }

    @Test
    @DisplayName("Deve atualizar conteudo preservando o id do documento MongoDB")
    void deveAtualizarConteudoPreservandoIdMongo() {
        Long aulaId = 4L;
        Aula aula = Aula.builder().id(aulaId).build();
        ConteudoAula existente = ConteudoAula.builder()
                .id("mongo-id-imutavel")
                .aulaId(aulaId)
                .corpoMarkdown("Markdown Antigo")
                .build();

        var request = new ConteudoAulaRequest("MARKDOWN", "Markdown Atualizado", List.of("https://novo.link"));

        when(aulaService.findAulaEntityById(aulaId)).thenReturn(aula);
        when(conteudoRepository.findByAulaId(aulaId)).thenReturn(Optional.of(existente));
        when(conteudoRepository.save(any(ConteudoAula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConteudoAula resultado = conteudoService.update(aulaId, request);

        assertNotNull(resultado);
        assertEquals("mongo-id-imutavel", resultado.getId());
        assertEquals(aulaId, resultado.getAulaId());
        assertEquals("Markdown Atualizado", resultado.getCorpoMarkdown());

        ArgumentCaptor<ConteudoAula> captor = ArgumentCaptor.forClass(ConteudoAula.class);
        verify(conteudoRepository).save(captor.capture());
        assertEquals("mongo-id-imutavel", captor.getValue().getId());
    }

    @Test
    @DisplayName("Deve deletar conteudo por aulaId com sucesso")
    void deveDeletarConteudoPorAulaIdComSucesso() {
        Long aulaId = 5L;
        Aula aula = Aula.builder().id(aulaId).build();
        ConteudoAula conteudo = ConteudoAula.builder().id("mongo-del").aulaId(aulaId).build();

        when(aulaService.findAulaEntityById(aulaId)).thenReturn(aula);
        when(conteudoRepository.findByAulaId(aulaId)).thenReturn(Optional.of(conteudo));

        conteudoService.deleteByAulaId(aulaId);

        verify(conteudoRepository).deleteByAulaId(aulaId);
    }

    @Test
    @DisplayName("Deve listar conteudos paginados com sucesso")
    void deveListarConteudosPaginadosComSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        ConteudoAula c1 = ConteudoAula.builder().id("c1").aulaId(1L).build();
        when(conteudoRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(c1)));

        Page<ConteudoAula> resultado = conteudoService.findAll(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(conteudoRepository).findAll(pageable);
    }
}
