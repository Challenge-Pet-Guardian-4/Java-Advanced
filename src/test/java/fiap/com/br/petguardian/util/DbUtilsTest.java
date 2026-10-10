package fiap.com.br.petguardian.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbUtilsTest {

    @Test
    @DisplayName("Deve extrair int corretamente de chave existente")
    void deveExtrairIntDeChaveExistente() {
        Map<String, Object> map = Map.of("pontos", 42, "longVal", 100L);
        assertEquals(42, DbUtils.getInt(map, "pontos"));
        assertEquals(100, DbUtils.getInt(map, "longVal"));
    }

    @Test
    @DisplayName("Deve retornar 0 para mapa nulo, vazio ou valor nao numerico")
    void deveRetornarZeroCasosBorda() {
        assertEquals(0, DbUtils.getInt(null, "chave"));
        assertEquals(0, DbUtils.getInt(Map.of(), "chave"));
        assertEquals(0, DbUtils.getInt(Map.of("chave", "stringInvalida"), "chave"));
    }
}
