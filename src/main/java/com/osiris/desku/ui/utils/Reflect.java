package com.osiris.desku.ui.utils;

import com.google.gson.Strictness;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Reflect {

    /**
     * Reversed version of {@link JsonWriter#REPLACEMENT_CHARS}.
     */
    private static final Map<String, Character> REPLACEMENT_CHARS = new HashMap<>();
    private static final Map<String, Character> HTML_SAFE_REPLACEMENT_CHARS;
    static {
        for (int i = 0; i <= 0x1f; i++) {
            REPLACEMENT_CHARS.put(String.format("\\u%04x", i), (char) i);
        }
        REPLACEMENT_CHARS.putAll(Map.of(
                "\\\"", '"',
                "\\\\", '\\',
                "\\t", '\t',
                "\\b", '\b',
                "\\n", '\n',
                "\\r", '\r',
                "\\f", '\f'
        ));

        HTML_SAFE_REPLACEMENT_CHARS = new HashMap<>();
        HTML_SAFE_REPLACEMENT_CHARS.putAll(REPLACEMENT_CHARS);
        HTML_SAFE_REPLACEMENT_CHARS.put("\\u003c", '<');
        HTML_SAFE_REPLACEMENT_CHARS.put("\\u003e", '>');
        HTML_SAFE_REPLACEMENT_CHARS.put("\\u0026", '&');
        HTML_SAFE_REPLACEMENT_CHARS.put("\\u003d", '=');
        HTML_SAFE_REPLACEMENT_CHARS.put("\\u0027", '\'');
    }

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

        /**
         * The direct reverse of {@link com.google.gson.JsonElement#toString()}
         * doesn't work because it treats the string as a Json element. Thus do the un-escaping of json manually.
         */
        pseudoPrimitivesAndParsers.put(String.class, asString -> {
            if(asString.contains("\\\\"))
                System.out.println();
            return asString;
        });
    }

    public static boolean isPseudoPrimitiveType(Object obj) {
        Class<?> aClass = obj.getClass();
        return aClass.isPrimitive() || pseudoPrimitivesAndParsers.containsKey(aClass);
    }

    public static boolean isPseudoPrimitiveType(Class<?> clazz) {
        return clazz.isPrimitive() || pseudoPrimitivesAndParsers.containsKey(clazz);
    }
}
