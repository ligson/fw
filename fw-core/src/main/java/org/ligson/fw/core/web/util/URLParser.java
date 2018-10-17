package org.ligson.fw.core.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URLParser {
    public static String getPath(String url) {
        if (url.contains("?")) {
            int endIdx = url.indexOf("?");
            return url.substring(0, endIdx);
        } else {
            return url;
        }
    }

    public static Map<String, List<String>> getParam(String url) {
        Map<String, List<String>> param = new HashMap<>();
        if (url.contains("?")) {
            int endIdx = url.indexOf("?");
            String[] arrays = url.substring(endIdx+1).split("&");
            for (String array : arrays) {
                List<String> values = new ArrayList<>();
                if (array.contains("=")) {
                    int idx = array.indexOf("=");
                    String key = array.substring(0, idx);
                    String value = array.substring(idx + 1);
                    if (param.containsKey(key)) {
                        param.get(key).add(value);
                    } else {
                        values.add(value);
                        param.put(key, values);
                    }
                } else {
                    if (!param.containsKey(array)) {
                        param.put(array, values);
                    }
                }
            }
            return param;
        } else {
            return param;
        }
    }

    public static void main(String[] args) {
        String url = "/user/register?name=sss&password=sdf&password=111&flag&flag";
        System.out.println(getPath(url));
        System.out.println(getParam(url));
    }
}
