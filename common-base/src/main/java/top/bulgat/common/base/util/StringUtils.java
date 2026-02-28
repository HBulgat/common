package top.bulgat.common.base.util;

/**
 * 字符串工具类。
 */
public final class StringUtils {

    private StringUtils() {
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * 驼峰命名转下划线命名。例如 userName -> user_name
     */
    public static String camelToUnderscore(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length() + 8);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 下划线命名转驼峰命名。例如 user_name -> userName
     */
    public static String underscoreToCamel(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        boolean upperNext = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                upperNext = true;
            } else {
                if (upperNext) {
                    sb.append(Character.toUpperCase(c));
                    upperNext = false;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    public static String capitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String uncapitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
