package fiap.com.br.petguardian.util;

import java.util.Map;

public final class DbUtils {

    private DbUtils() {
    }

    public static int getInt(Map<String, Object> map, String key) {
        if (map == null || key == null) return 0;
        Object val = map.get(key);
        if (val == null) {
            val = map.get(key.toUpperCase());
        }
        if (val == null) {
            val = map.get(key.toLowerCase());
        }
        return val instanceof Number n ? n.intValue() : 0;
    }

    public static String getString(Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        Object val = map.get(key);
        if (val == null) {
            val = map.get(key.toUpperCase());
        }
        if (val == null) {
            val = map.get(key.toLowerCase());
        }
        return val != null ? val.toString() : null;
    }
}
