package com.osiris.desku.ui.event;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.utils.NoValue;
import com.osiris.desku.ui.utils.Reflect;
import com.osiris.jlib.json.JsonFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ValueChangeEvent<COMP extends Component<?, VALUE>, VALUE> extends JavaScriptEvent<COMP> {

    public static <T> T stringToVal(String value, Component<?,T> comp){
        if(value == null || value.isEmpty()){
            return comp.internalDefaultValue;
        }
        else if(Reflect.isPseudoPrimitiveType(comp.internalValueClass))
            return (T) Reflect.pseudoPrimitivesAndParsers.get(comp.internalValueClass)
                    .apply(value);
        else
            return JsonFile.parser.fromJson(value, comp.internalValueClass);
    }

    public static <T> T jsonElToVal(JsonElement value, Component<?, T> comp){
        if(value.isJsonPrimitive()){
            return stringToVal(value.getAsString(), comp);
        } else
            return JsonFile.parser.fromJson(value, comp.internalValueClass);
    }

    /**
     * How the value is saved/shown on the client-side/UI. <br>
     * Is safe in JSON, and maybe safe to use in HTML attributes (since we hope Jsoup handles escaped strings properly).  <br><br>
     *
     * There are multiple possibilities: <br><br>
     *
     * 1. The value type is a primitive or pseudo-primitive (uppercase class of a primitive), like a number, in that case it can be simply converted to a string
     * and set in HTML value attribute. <br><br>
     *
     * 2. The value type is a string. In this case the string might contain special chars, thus we escape it using the gson library. <br><br>
     *
     * 3. The value type is neither of the above, meaning a custom type, in which case its converted into a json string,
     * and escaping the fields/values of that json object is handled by the gson library. <br><br>
     *
     * 4. The value is null or of type {@link com.osiris.desku.ui.utils.NoValue}, thus return an empty string. <br><br>
     */
    public static <VALUE> @NotNull String valToString(@Nullable VALUE val, @NotNull Component<?, VALUE> comp){
        if(val == null || val instanceof NoValue) return "";
        else if(val instanceof String){ // Escapes the string if needed, so that it can be used in json or html attributes
            return escapeString((String) val);
        }
        else if(Reflect.isPseudoPrimitiveType(val)){
            return String.valueOf(val);
        } else{
            String s = JsonFile.parser.toJson(val, comp.internalValueClass);
            if(s.equals("null") || s.isEmpty())
                throw new RuntimeException("Cannot convert value of type inline class ("+comp.internalValueClass+") into a json string, unsupported by gson at the moment!" +
                        " Move your class outside of your method or runnable into its parent class, or directly create a new file for it.");
            return s;
        }
    }

    public final VALUE value;
    public final VALUE valueBefore;

    /**
     * @param rawJSMessage expected in this format: <br>
     *                     {"newValue": "...", "eventAsJson": {...}}
     * @param comp
     */
    public ValueChangeEvent(String rawJSMessage, COMP comp, VALUE valueBefore) {
        super(rawJSMessage, comp);
        this.valueBefore = valueBefore;
        this.value = jsonElToVal(jsMessage.get("newValue"), comp);
    }

    /**
     * Prevent JSON->JAVA parsing of {@link #rawJSMessage} and {@link #value} by using this constructor instead.
     */
    public ValueChangeEvent(String rawJSMessage, JsonObject jsMessage, COMP comp, VALUE value, VALUE valueBefore, boolean isProgrammatic) {
        super(rawJSMessage, jsMessage, comp, isProgrammatic);
        this.value = value;
        this.valueBefore = valueBefore;
    }

    public String getValueAsString(){
        return jsMessage.get("newValue").getAsString();
    }

    public String getValueAsEscapedString(){
        String s = getValueAsString();
        return escapeString(s);
    }

    public static String escapeString(String s){
        s = new JsonPrimitive(s).toString();
        s = s.substring(1, s.length()-1); // remove encapsulating ""
        return s;
    }

}
