package fiap.com.br.petguardian.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DbUtilsTest {

    @Test
    @DisplayName("Deve extrair int corretamente de chave existente")
    void deveExtrairIntDeChaveExistente() {
        Map<String, Object> map = Map.of("pontos", 42, "longVal", 100L);
        assertEquals(42, DbUtils.getInt(map, "pontos"));
        assertEquals(100, DbUtils.getInt(map, "longVal"));
    }

    @Test
    @DisplayName("Deve extrair int com chave em maiusculas mesmo que mapa esteja em maiusculas ou minusculas")
    void deveExtrairIntComChaveEmMaiusculasEMinusculas() {
        Map<String, Object> mapUpper = Map.of("P_PONTOS_TAREFAS", 50, "P_TAREFAS_PENDENTES", 3);
        assertEquals(50, DbUtils.getInt(mapUpper, "P_PONTOS_TAREFAS"));
        assertEquals(50, DbUtils.getInt(mapUpper, "p_pontos_tarefas"));
        assertEquals(3, DbUtils.getInt(mapUpper, "p_tarefas_pendentes"));

        Map<String, Object> mapLower = Map.of("p_pontos_tarefas", 80);
        assertEquals(80, DbUtils.getInt(mapLower, "P_PONTOS_TAREFAS"));
        assertEquals(80, DbUtils.getInt(mapLower, "p_pontos_tarefas"));
    }

    @Test
    @DisplayName("Deve retornar 0 para mapa nulo, vazio ou valor nao numerico")
    void deveRetornarZeroCasosBorda() {
        assertEquals(0, DbUtils.getInt(null, "chave"));
        assertEquals(0, DbUtils.getInt(Map.of(), "chave"));
        assertEquals(0, DbUtils.getInt(Map.of("chave", "stringInvalida"), "chave"));
    }

    @Test
    @DisplayName("Deve extrair string com chave em maiusculas, minusculas ou exata, e null para bordas")
    void deveExtrairStringComFallback() {
        Map<String, Object> mapUpper = Map.of("P_DETALHE_PETS_JSON", "[{\"petId\":1}]");
        assertEquals("[{\"petId\":1}]", DbUtils.getString(mapUpper, "p_detalhe_pets_json"));
        assertEquals("[{\"petId\":1}]", DbUtils.getString(mapUpper, "P_DETALHE_PETS_JSON"));

        Map<String, Object> mapLower = Map.of("p_detalhe_pets_json", "[]");
        assertEquals("[]", DbUtils.getString(mapLower, "P_DETALHE_PETS_JSON"));
        assertEquals("[]", DbUtils.getString(mapLower, "p_detalhe_pets_json"));

        assertNull(DbUtils.getString(null, "chave"));
        assertNull(DbUtils.getString(Map.of(), "chave"));
        assertNull(DbUtils.getString(Map.of("outra", "valor"), "chave"));
    }
}
