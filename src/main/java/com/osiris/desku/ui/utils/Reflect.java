package com.osiris.desku.ui.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Reflect {
    public static final Map<Class<?>, Function<String, ?>> pseudoPrimitivesAndParsers;
    static {
        pseudoPrimitivesAndParsers = new HashMap<Class<?>, Function<String, ?>>(18);
        pseudoPrimitivesAndParsers.put(Integer.class, asString -> {
            return Integer.parseInt(asString);
        });
        pseudoPrimitivesAndParsers.put(Byte.class, asString -> {
            return Byte.parseByte(asString);
        });
        pseudoPrimitivesAndParsers.put(Character.class, asString -> {
            return asString.charAt(0);
        });
        pseudoPrimitivesAndParsers.put(Boolean.class, asString -> {
            return Boolean.parseBoolean(asString);
        });
        pseudoPrimitivesAndParsers.put(Double.class, asString -> {
            return Double.parseDouble(asString);
        });
        pseudoPrimitivesAndParsers.put(Float.class, asString -> {
            return Float.parseFloat(asString);
        });
        pseudoPrimitivesAndParsers.put(Long.class, asString -> {
            return Long.parseLong(asString);
        });
        pseudoPrimitivesAndParsers.put(Short.class, asString -> {
            return Short.parseShort(asString);
        });
        pseudoPrimitivesAndParsers.put(String.class, asString -> {
            return asString;
        });
    }

    public static boolean isPseudoPrimitiveType(Object obj) {
        Class<?> aClass = obj.getClass();
        return aClass.isPrimitive() || pseudoPrimitivesAndParsers.containsKey(aClass);
    }
}
