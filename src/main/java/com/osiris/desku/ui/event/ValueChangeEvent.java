package com.osiris.desku.ui.event;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.utils.Reflect;
import com.osiris.jlib.json.JsonFile;

public class ValueChangeEvent<COMP extends Component<?, VALUE>, VALUE> extends JavaScriptEvent<COMP> {

    public static <T> T getValueFromString(String value, Component<?,T> comp){
        if(value.isEmpty()){ // If empty can only be a string nothing else.
            return (T) "";
        }
        char c = value.charAt(0);
        if(c == '{' || c == '[') // NoValue.class for example is {} as JSON
            return getValueFromString(JsonFile.parser.toJson(value, comp.internalValueClass),
                    comp);
        else{
            return (T) Reflect.pseudoPrimitivesAndParsers.get(comp.internalValueClass)
                    .apply(value);
        }
    }

    public static <T> T getValueFromJsonEl(JsonElement value, Component<?, T> comp){
        if(value.isJsonPrimitive()){
            return getValueFromString(value.getAsString(), comp);
        } else
            return JsonFile.parser.fromJson(value, comp.internalValueClass);
    }

    /**
     * How the value is saved/shown on the client-side/UI.
     */
    public static <VALUE> String getStringFromValue(VALUE val, Component<?, VALUE> comp){
        if(Reflect.isPseudoPrimitiveType(val)){
            return String.valueOf(val);
        } else{
            return JsonFile.parser.toJson(val, comp.internalValueClass);
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
        this.value = getValueFromJsonEl(jsMessage.get("newValue"), comp);
    }

    /**
     * Prevent JSON->JAVA parsing of {@link #rawJSMessage} and {@link #value} by using this constructor instead.
     */
    public ValueChangeEvent(String rawJSMessage, JsonObject jsMessage, COMP comp, VALUE value, VALUE valueBefore) {
        super(rawJSMessage, jsMessage, comp);
        this.value = value;
        this.valueBefore = valueBefore;
    }


}
