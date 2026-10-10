package fiap.com.br.petguardian.util;

import java.util.Map;

public final class DbUtils {

    private DbUtils() {
    }

    public static int getInt(Map<String, Object> map, String key) {
        return map != null && map.get(key) instanceof Number n ? n.intValue() : 0;
    }
}
