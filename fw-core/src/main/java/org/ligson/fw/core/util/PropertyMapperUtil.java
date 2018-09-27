package org.ligson.fw.core.util;

/**
 * Created by ligson on 2017/8/4.
 */
public class PropertyMapperUtil {
    /***
     * userName->user_name
     * @param propertyName
     * @return
     */
    public static String convert2_(String propertyName) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < propertyName.length(); i++) {
            int ch = propertyName.charAt(i);
            if (Character.isUpperCase(ch)) {
                builder.append("_").append((char) Character.toLowerCase(ch));
            } else {
                builder.append((char) ch);
            }
        }
        return builder.toString();
    }

    /***
     * user_name->userName
     * @param columnName
     * @return
     */
    public static String convert2Asc(String columnName) {
        StringBuilder builder = new StringBuilder();
        boolean skip = false;
        for (int i = 0; i < columnName.length(); i++) {
            int ch = columnName.charAt(i);
            if (ch == '_') {
                if (i != (columnName.length() - 1)) {
                    builder.append((char) ch).append(Character.toUpperCase(columnName.charAt(i + 1)));
                    skip = true;
                } else {
                    skip = false;
                }
            } else {
                if ((!skip) && (!Character.isUpperCase(ch))) {
                    builder.append((char) ch);
                }
                skip = false;
            }
        }
        return builder.toString().replaceAll("_", "");
    }

    public static String convert2CamelCase(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    public static void main(String[] args) {
        System.out.println(convert2_("userNameSpace"));
        System.out.println(convert2Asc("user_name"));
    }
}
