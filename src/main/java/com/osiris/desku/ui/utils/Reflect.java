package com.osiris.desku.ui.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Reflect {
    public static final Map<Class<?>, Function<String, ?>> pseudoPrimitivesAndParsers;
    static {
        pseudoPrimitivesAndParsers = new HashMap<Class<?>, Function<String, ?>>(18);
        // Adding parsers for both wrapper classes and primitive types
        pseudoPrimitivesAndParsers.put(Integer.class, asString -> Integer.parseInt(asString));
        pseudoPrimitivesAndParsers.put(int.class, asString -> Integer.parseInt(asString));

        pseudoPrimitivesAndParsers.put(Byte.class, asString -> Byte.parseByte(asString));
        pseudoPrimitivesAndParsers.put(byte.class, asString -> Byte.parseByte(asString));

        pseudoPrimitivesAndParsers.put(Character.class, asString -> asString.charAt(0));
        pseudoPrimitivesAndParsers.put(char.class, asString -> asString.charAt(0));

        pseudoPrimitivesAndParsers.put(Boolean.class, asString -> Boolean.parseBoolean(asString));
        pseudoPrimitivesAndParsers.put(boolean.class, asString -> Boolean.parseBoolean(asString));

        pseudoPrimitivesAndParsers.put(Double.class, asString -> Double.parseDouble(asString));
        pseudoPrimitivesAndParsers.put(double.class, asString -> Double.parseDouble(asString));

        pseudoPrimitivesAndParsers.put(Float.class, asString -> Float.parseFloat(asString));
        pseudoPrimitivesAndParsers.put(float.class, asString -> Float.parseFloat(asString));

        pseudoPrimitivesAndParsers.put(Long.class, asString -> Long.parseLong(asString));
        pseudoPrimitivesAndParsers.put(long.class, asString -> Long.parseLong(asString));

        pseudoPrimitivesAndParsers.put(Short.class, asString -> Short.parseShort(asString));
        pseudoPrimitivesAndParsers.put(short.class, asString -> Short.parseShort(asString));

        pseudoPrimitivesAndParsers.put(String.class, asString -> asString);
    }

    public static boolean isPseudoPrimitiveType(Object obj) {
        Class<?> aClass = obj.getClass();
        return aClass.isPrimitive() || pseudoPrimitivesAndParsers.containsKey(aClass);
    }

    public static boolean isPseudoPrimitiveType(Class<?> clazz) {
        return clazz.isPrimitive() || pseudoPrimitivesAndParsers.containsKey(clazz);
    }
}
