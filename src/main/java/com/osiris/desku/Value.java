package com.osiris.desku;

import com.google.gson.JsonElement;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.utils.NoValue;
import com.osiris.desku.ui.utils.Reflect;
import com.osiris.jlib.json.JsonFile;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Entities;

/**
 * There are various places a value can exist. <br>
 * 1. In Java {@link com.osiris.desku.ui.Component#internalValue} <br>
 * 2. In Java Jsoup value attribute as JSON or JSON Primitive both in String {@link org.jsoup.nodes.Element#attr(String)} <br>
 * 3. In Java during transmission to client as JSON (Object) or JSON Primitive (String) {@link com.osiris.desku.ui.event.ValueChangeEvent#message} <br>
 * 4. In JavaScript as JSON or JSON Primitive both in String at client. <br>
 * 5. In HTML value attribute at client as JSON or JSON Primitive both in String. <br>
 */
public class Value {

    public static String escapeForJsoup(String s){
        return Entities.escape(s);
    }

    public static String unescapeForJsoup(String s){
        return Entities.unescape(s);
    }

    public static String escapeForJSON(String s){
        return StringEscapeUtils.escapeJson(s);
    }

    public static String unescapeForJSON(String s){
        return StringEscapeUtils.unescapeJson(s);
    }

    /**
     * Only escapes the ` char, thus expects you to embed/encapsulate the returned string in `
     * (and not ' or "), inside your JavaScript code.
     */
    public static String escapeForJavaScript(String s){
        return s.replace("`", "\\`");
    }

    public static String unescapeForJavaScript(String s){
        return s.replace("\\`", "`");
    }

    /**
     * Convert value to a string. <br>
     * THE RETURNED VALUE IS NOT ESCAPED. <br><br>
     *
     * There are multiple possibilities: <br><br>
     *
     * 1. The value type is a primitive or pseudo-primitive (uppercase class of a primitive), like a number, in that case it can be simply converted to a string
     * and set in HTML value attribute. <br><br>
     *
     * 2. The value type is a string, return directly. <br><br>
     *
     * 3. The value type is neither of the above, meaning a custom type, in which case its converted into a json string. <br><br>
     *
     * 4. The value is null or of type {@link com.osiris.desku.ui.utils.NoValue}, thus return an empty string. <br><br>
     */
    public static <VALUE> @NotNull String valToString(@Nullable VALUE val, @NotNull Component<?, VALUE> comp){
        String s = "";
        if(val == null || val instanceof NoValue) s =  "";
        else if(val instanceof String){
            s = (String) val;
        }
        else if(Reflect.isPseudoPrimitiveType(val)){
            s = String.valueOf(val);
        } else{
            s = JsonFile.parser.toJson(val, comp.internalValueClass);
            if(s.equals("null") || s.isEmpty())
                throw new RuntimeException("Cannot convert value of type inline class ("+comp.internalValueClass+") into a json string, unsupported by gson at the moment!" +
                        " Move your class outside of your method or runnable into its parent class, or directly create a new file for it.");
        }
        return s;
    }

    /**
     * NOTE THAT UNESCAPING MUST BE PERFORMED BY YOU BEFORE EXECUTING THIS, IF ESCAPING WAS DONE.
     */
    public static <T> T stringToVal(String s, Component<?,T> comp){
        if(s == null || s.isEmpty()){
            return comp.internalDefaultValue;
        }
        if(Reflect.isPseudoPrimitiveType(comp.internalValueClass))
            return (T) Reflect.pseudoPrimitivesAndParsers.get(comp.internalValueClass)
                    .apply(s);
        else{
            return JsonFile.parser.fromJson(s, comp.internalValueClass);
        }
    }

    /**
     * NOTE THAT UNESCAPING MUST BE PERFORMED BY YOU BEFORE EXECUTING THIS, IF ESCAPING WAS DONE.
     */
    public static <T> T jsonElToVal(JsonElement el, Component<?, T> comp){
        if(el.isJsonPrimitive()){
            return stringToVal(el.getAsString(), comp);
        } else
            return JsonFile.parser.fromJson(el, comp.internalValueClass);
    }
}
