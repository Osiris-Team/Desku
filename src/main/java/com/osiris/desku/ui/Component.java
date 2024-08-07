package com.osiris.desku.ui;

import com.osiris.desku.App;
import com.osiris.desku.ui.css.CSS;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ClickEvent;
import com.osiris.desku.ui.event.ScrollEvent;
import com.osiris.desku.ui.event.ValueChangeEvent;
import com.osiris.desku.ui.layout.*;
import com.osiris.desku.utils.GodIterator;
import com.osiris.events.Event;
import com.osiris.jlib.json.JsonFile;
import com.osiris.jlib.logger.AL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Graphical representation of a single value/object. <br>
 * Can also be used as something else that doesn't require a value (like a container for example). <br>
 * Can be added to any {@link com.osiris.desku.Route}. <br>
 * @param <THIS> reference to itself to allow correct method-chaining in extending classes. See also {@link #_this} variable.
 * @param <VALUE> type of the value/data this Component is representing. See also {@link #getValue(Consumer)}, {@link #setValue(Object)},
 *               {@link #onValueChange(Consumer)}.
 */
public class Component<THIS extends Component<THIS, VALUE>, VALUE> {
    private static final AtomicInteger idCounter = new AtomicInteger();

    static {
        try {
            String styles = "#outlet * {\n" +
                    "  display: flex;\n" +  // All children of outlet will be flex
                    "}\n";
            App.appendToGlobalCSS(styles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Equals the attribute "java-id" inside HTML and thus useful for finding this object via JavaScript. <br>
     * Example: The code below will return the object with the java-id = 5.
     * <pre>
     *     var element = document.querySelectorAll('[java-id="5"]')[0];
     * </pre>
     */
    public final int id = idCounter.getAndIncrement();
    public final CopyOnWriteArrayList<Component> children = new CopyOnWriteArrayList<>();
    /**
     * Executed when a child was added on the Java side. <br>
     *
     * @see AddedChildEvent
     */
    public final Event<AddedChildEvent> onChildAdd = new Event<>();
    /**
     * Executed when a child was removed on the Java side.
     */
    public final Event<Component<?,?>> onChildRemove = new Event<>();
    /**
     * Executed when a style change was made on the Java side.
     */
    public final Event<Attribute> onStyleChange = new Event<>();
    /**
     * Executed when a attribute change was made on the Java side. <br>
     * Note that style changes are handled by {@link #onStyleChange}. <br>
     */
    public final Event<AttributeChangeEvent> onAttributeChange = new Event<>();
    /**
     * Do not add actions via this variable because it needs additional UI-side JavaScript event registration,
     * use {@link #onClick(Consumer)} instead.
     */
    public final Event<ClickEvent> readOnlyOnClick = new Event<>();
    /**
     * Do not add actions via this variable because it needs additional UI-side JavaScript event registration,
     * use {@link #onScroll(Consumer)} instead.
     */
    public final Event<ScrollEvent> readOnlyOnScroll = new Event<>();
    /**
     * Do not add actions via this variable because it needs additional UI-side JavaScript event registration,
     * use {@link #onValueChange(Consumer)}} instead.
     */
    public final Event<ValueChangeEvent<THIS, VALUE>> readOnlyOnValueChange = new Event<>();
    /**
     * Gets executed when this component <br>
     * was attached to the UI.
     */
    public final Event<Void> _onAttached = new Event<>();
    public final ConcurrentHashMap<String, String> style = new ConcurrentHashMap<>();
    /**
     * Gets set to false in {@link AddedChildEvent}. <br>
     */
    public boolean isFirstAdd = true;
    /**
     * True if this component is attached to a UI. <br>
     * Gets set to false if this component was removed. <br>
     */
    private boolean isAttached = false;

    public THIS setAttached(boolean b){
        isAttached = b;
        _onAttached.execute(null);
        return _this;
    }
    public boolean isAttached(){
        return isAttached;
    }
    /**
     * The instance of the extending class. <br>
     * Is returned in pretty much all methods, to allow method chaining by returning
     * the extending class instead of {@link Component}.
     */
    public THIS _this = (THIS) this;
    public VALUE internalValue;
    public VALUE internalDefaultValue;
    public Class<VALUE> internalValueClass;
    /**
     * Jsoup {@link Element} that can be used to convert this
     * {@link Component} into an actual HTML string.
     * <u>
     * Note that changes to this element are only visible in the UI if
     * they are done before it gets loaded the first time. <br>
     * This means that changes to this element made in a click event for example
     * will not be visible in the UI. <br>
     * Use wrapper methods like {@link #innerHTML(String)} for example.
     * </u>
     */
    public MyElement element;
    public Consumer<Component> _remove = child -> {
        UI ui = UI.get(); // Necessary for updating the actual UI via JavaScript
        if (children.contains(child)) {
            children.remove(child);
            if (child.element.parent() != null)
                child.element.remove();
            child.update();

            // Update UI
            if (isAttached && !ui.isLoading()){
                ui.executeJavaScriptSafely(ui.jsGetComp("comp", id) +
                                ui.jsGetComp("childComp", child.id) +
                                "comp.removeChild(childComp);\n",
                        "internal", 0);
            }

            child.isAttached = false;
            onChildRemove.execute(child);
        }
    };
    public Consumer<Component> _removeSelf = self -> {
        UI ui = UI.get(); // Necessary for updating the actual UI via JavaScript
        if (self.element.parent() != null){
            self.element.remove();
        }
        self.update();

        // Update UI
        if (isAttached && !ui.isLoading()){
            ui.executeJavaScriptSafely(ui.jsGetComp("comp", self.id) +
                            "comp.parentNode.removeChild(comp);\n",
                    "internal", 0);
        }

        self.isAttached = false;
        //onChildRemove.execute(self);
    };
    public Consumer<AddedChildEvent> _add = (e) -> {

        // Perform in-memory update
        if (e.otherChildComp == null) { // add
            children.add(e.childComp);
            e.childComp.update();
            element.appendChild(e.childComp.element);
        } else if (e.isInsert) {
            int iOtherComp = children.indexOf(e.otherChildComp);
            children.set(iOtherComp, e.childComp);
            e.childComp.update();
            element.insertChildren(iOtherComp, e.childComp.element);
        } else if (e.isReplace) {
            int iOtherComp = children.indexOf(e.otherChildComp);
            children.set(iOtherComp, e.childComp);
            e.childComp.update();
            element.insertChildren(iOtherComp, e.childComp.element);
        }

        // Perform actual UI update
        UI ui = UI.get();
        if (!ui.isLoading()){
            if(!this.isAttached) {
                // Means that this (parent) was not attached yet,
                // thus we postpone the addition of child to the end of UI.access()
                ui.attachWhenAccessEnds(this, e.childComp, e);
            } else{
                ui.attachToParent(this, e.childComp, e);
            }
        }

        // Execute listeners
        onChildAdd.execute(e);
        if (e.isReplace) _remove.accept(e.otherChildComp);// Removes from children, (node) children, and actual UI
    };
    public Consumer<Attribute> _styleChange = attribute -> {
        UI ui = UI.get(); // Necessary for updating the actual UI via JavaScript
        if (attribute.getValue().isEmpty()) {

            // Remove style
            style.remove(attribute.getKey());
            String style = element.hasAttr("style") ? element.attributes().get("style") : "";
            int iKeyFirstChar = style.indexOf(attribute.getKey());
            if (iKeyFirstChar == -1) return; // Already doesn't exist, so no removal is needed
            style = style.substring(0, iKeyFirstChar) + style.substring(style.indexOf(";", iKeyFirstChar) + 1);
            element.attr("style", style); // Change in-memory representation

            // Update UI
            if (isAttached && !ui.isLoading()){
                executeJS("comp.style." + CSS.getJSCompatibleCSSKey(attribute.getKey())
                        + " = ``;\n"); // Change UI representation
            }
        } else {

            // Add or change style
            style.put(attribute.getKey(), attribute.getValue());

            String style = element.hasAttr("style") ? element.attributes().get("style") : "";
            style += attribute.getKey() + ": " + attribute.getValue() + ";";
            element.attr("style", style); // Change in-memory representation

            // Update UI
            if (isAttached && !ui.isLoading()){
                executeJS("comp.style." + CSS.getJSCompatibleCSSKey(attribute.getKey())
                        + " = `" + attribute.getValue() + "`;\n"); // Change UI representation
            }
        }
        onStyleChange.execute(attribute);
    };
    public Consumer<AttributeChangeEvent> _attributeChange = e -> {
        UI ui = UI.get(); // Necessary for updating the actual UI via JavaScript
        if (e.isInsert) { // Add or change attribute
            element.attr(e.attribute.getKey(), e.attribute.getValue()); // Change in-memory representation
            if (isAttached && !ui.isLoading()){
                executeJS("comp.setAttribute(`" + e.attribute.getKey()
                        + "`, `" + e.attribute.getValue() + "`);\n" +
                        "comp[\""+e.attribute.getKey()+"\"] = `"+e.attribute.getValue()+"`"); // Change UI representation
            }

        } else {// Remove attribute
            element.removeAttr(e.attribute.getKey()); // Change in-memory representation
            if (isAttached && !ui.isLoading()){
                executeJS("comp.removeAttribute(`" + e.attribute.getKey() + "`);\n" +
                        "comp[\""+e.attribute.getKey()+"\"] = null"); // Change UI representation
            }
        }
        onAttributeChange.execute(e);
    };
    boolean changedAddToSupportScroll = false;

    /**
     * @see #Component(Object, Class, String)
     */
    public Component(@NotNull VALUE value, @NotNull Class<VALUE> valueClass) {
        this(value, valueClass, "c");
    }

    /**
     * @param value default starting value for this component. For compatibility reasons null is allowed, however note that if that's the case
     *              a default value will be created using reflection, if that doesn't work a RuntimeException is thrown.
     * @param valueClass the class of the default value.
     * @param tag html tag.
     */
    public Component(@UnknownNullability VALUE value, @NotNull Class<VALUE> valueClass, @NotNull String tag) {
        if(value == null) {
            if(valueClass == String.class) value = (VALUE) "";
            else {
                try {
                    if (!Modifier.isAbstract(valueClass.getModifiers()) && valueClass.getDeclaredConstructor() != null) {
                        value = valueClass.getDeclaredConstructor().newInstance();
                    } else {
                        throw new IllegalArgumentException("Class must have a public no-argument constructor and not be abstract.");
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException("Failed to create default value: " + e.getMessage(), e);
                }
            }
        }
        this.internalValue = value;
        this.internalDefaultValue = value;
        this.internalValueClass = valueClass;
        this.element = new MyElement(this, tag);
        element.attr("java-id", String.valueOf(id));
        atr("value", ValueChangeEvent.valToString(value, this));
        // Do not use setValue since that might be overwritten by extending class and thus cause issues
    }

    /**
     * Util method to get the value directly. <br>
     * Thus override {@link #getValue(Consumer)} instead if needed.
     */
    public @NotNull VALUE getValue() {
        AtomicReference<VALUE> atomicValue = new AtomicReference<>();
        getValue(val -> {
            atomicValue.set(val);
        });
        while(atomicValue.get() == null) Thread.yield(); // Wait until value returned
        return atomicValue.get();
    }

    /**
     * @param v executed when the value is got from the client-side.
     * @return should never return null, even if setValue(null) was called, in that case it returns the {@link #internalDefaultValue}
     * that was set in the constructor.
     */
    public THIS getValue(Consumer<@NotNull VALUE> v) {
        UI ui = UI.get();
        if(ui == null || ui.isLoading()) // Since never attached once, user didn't have a chance to change the value, thus return internal directly
            v.accept(internalValue);
        else
            gatr("value", valueAsString -> {
                VALUE value = ValueChangeEvent.stringToVal(valueAsString, this);
                v.accept(value);
            });
        return _this;
    }

    /**
     * Sets {@link #internalValue} AND triggers the {@link #readOnlyOnValueChange} event,
     * meaning client-side is also affected.
     * @see ValueChangeEvent#valToString(Object, Component)
     * @see ValueChangeEvent#stringToVal(String, Component)
     */
    public THIS setValue(@Nullable VALUE v) {
        String newVal = ValueChangeEvent.valToString(v, this);

        String newValJsonSafe;
        if(v instanceof String) newValJsonSafe = "\""+newVal+"\"";
        else if(newVal.isEmpty()) newValJsonSafe = "\"\"";
        else newValJsonSafe = newVal; // json object or other primitive

        String json = "{\"newValue\": "+newValJsonSafe+"}";
        ValueChangeEvent<THIS, VALUE> event = new ValueChangeEvent<>(json, _this, this.internalValue);
        event.isProgrammatic = true;
        this.internalValue = v;

        atr("value", newVal); // Change in memory value, without triggering another change event
        readOnlyOnValueChange.execute(event); // Executes all listeners

        return _this;
    }

    /**
     * Adds a listener that gets executed when the value was changed either programmatically via {@link #setValue(Object)}
     * or by the user (by listening to the input event).
     *
     * @see UI#registerJSListener(String, Component, String, Consumer)
     * @see ValueChangeEvent#valToString(Object, Component)
     * @see ValueChangeEvent#stringToVal(String, Component)
     */
    public THIS onValueChange(Consumer<ValueChangeEvent<THIS, VALUE>> code) {
        readOnlyOnValueChange.addAction((event) -> code.accept(event));
        UI.get().registerJSListener("input", this,
                "message = `{\"newValue\": \"` + event.target.value.escapeSpecialChars() + `\", \"eventAsJson\":` + message + `}`;\n",
                (msg) -> {
                    ValueChangeEvent<THIS, VALUE> event = new ValueChangeEvent<>(msg, _this, internalValue);
                    VALUE newValue = event.value; // msg contains the new data and is parsed above in the event constructor
                    internalValue = newValue; // Change in memory value, without triggering another change event
                    atr("value", event.getValueAsEscapedString());
                    readOnlyOnValueChange.execute(event); // Executes all listeners
                });
        // TODO also register programmatic value change listener
        return _this;
    }

    public boolean isDefaultValue(){
        return isValuesEqual(getValue(), this.internalDefaultValue);
    }

    /**
     * Checks if val1 and val2 are equal, if not additionally converts them to json and checks them:
     * Each of those subclasses (JsonObject, JsonArray, etc.) overrides the Object.equals method, providing an effective deep JSON comparison.
     * Meaning we compare if the fields and their values are equal, regardless of order.
     * @see ValueChangeEvent#valToString(Object, Component)
     * @see ValueChangeEvent#stringToVal(String, Component)
     */
    public boolean isValuesEqual(VALUE val1, VALUE val2){
        if(val1 == val2) return true;
        return JsonFile.parser.toJsonTree(val1, this.internalValueClass)
                .equals(JsonFile.parser.toJsonTree(val2, this.internalValueClass));
    }

    /**
     * Executes the provided JavaScript code now, or later
     * if this component is not attached yet. <br>
     * Your code will be encapsulated in a try/catch block and errors logged to
     * the clients JavaScript console. <br>
     * A reference of this component will be added before your code, thus you can access
     * this component via the "comp" variable in your provided JavaScript code.
     */
    public THIS executeJS(String code){
        return executeJS(UI.get(), code);
    }

    /**
     * @see #executeJS(String)
     */
    public THIS executeJS(UI ui, String code){
        if(isAttached){
            ui.executeJavaScriptSafely(
                    "try{"+
                            ui.jsGetComp("comp", id) +
                            code+
                            "}catch(e){console.error(e)}",
                    "internal", 0);
        } else{ // Execute code once attached
            _onAttached.addOneTimeAction((event, action) -> {
                ui.executeJavaScriptSafely(
                        "try{"+
                                ui.jsGetComp("comp", id) +
                                code+
                                "}catch(e){console.error(e)}",
                        "internal", 0);
            }, AL::warn);
        }
        return _this;
    }

    /**
     * Executes the provided JavaScript code now, or later if this component is not attached yet. <br>
     * Additional arguments make it possible to execute Java code, once your provided JavaScript code
     * finishes execution. Also enables you to pass over Strings from JavaScript to Java,
     * by setting the message variable in JavaScript code. <br>
     * @see UI#jsAddPermanentCallback(String, Consumer, Consumer)
     */
    public THIS executeJS(String code, Consumer<String> onSuccess, Consumer<String> onError){
        return executeJS(UI.get(), code, onSuccess, onError);
    }

    /**
     * @see #executeJS(String, Consumer, Consumer)
     */
    public THIS executeJS(UI ui, String code, Consumer<String> onSuccess, Consumer<String> onError){
        code = ui.jsAddPermanentCallback(code, onSuccess, onError);
        executeJS(code);
        return _this;
    }

    /**
     * Executes the provided code synchronously in the current thread. <br>
     *
     * @param code the code to be executed now, contains this component as parameter.
     */
    public THIS now(Consumer<THIS> code) {
        code.accept(_this);
        return _this;
    }

    /**
     * Executes the provided code asynchronously in a new thread. <br>
     * This function needs to be run inside UI context
     * since it executes {@link UI#get()}, otherwise {@link NullPointerException} is thrown. <br>
     * <br>
     * Note that your code-block will have access to the current UI,
     * which means that you can add/remove/change UI components without issues.
     * This also means that you will have to handle Thread-safety yourself
     * when doing things to the same component from multiple threads at the same time.
     *
     * @param code the code to be executed asynchronously, contains this component as parameter.
     */
    public THIS later(Consumer<THIS> code) {
        UI ui = UI.get();
        Objects.requireNonNull(ui);
        App.executor.execute(() -> {
            ui.access(() -> code.accept(_this));
        });
        return _this;
    }

    /**
     * Same as {@link #later(Consumer)} but
     * adds an overlay that
     * shows the text "Loading..." and dims/darkens this component,
     * until the async task finishes.
     */
    public THIS laterWithOverlay(BiConsumer<THIS, Overlay> code) {
        later(_this -> {
            Overlay overlay = new Overlay(this)
                    .childCenter1().sty("background", "rgba(0,0,0,0.3)")
                    .sizeFull();
            add(overlay);
            overlay.add(new Text("Loading...")
                    .sty("color", "white").sizeXL().selfCenter2());
            overlay.add(new Text("This might take a while, please be patient.")
                    .sty("color", "white").sizeS().selfCenter2());
            code.accept(this._this, overlay);
            remove(overlay);
        });
        return _this;
    }

    public THIS add(Iterable<Component<?,?>> comps) {
        if (comps == null) return _this;
        GodIterator.forEach(comps, c -> {
            _add.accept(new AddedChildEvent(c, null, false, false));
        });
        return _this;
    }

    public THIS add(Component<?,?>... comps) {
        if (comps == null) return _this;
        GodIterator.forEach(comps, c -> {
            _add.accept(new AddedChildEvent(c, null, false, false));
        });
        return _this;
    }

    /**
     * @throws IndexOutOfBoundsException
     */
    public THIS addAt(int index, Component<?,?> comp) {
        if (comp == null) return _this;
        Component<?,?> otherChildComp = children.get(index);
        _add.accept(new AddedChildEvent(comp, otherChildComp, true, false));
        return _this;
    }

    /**
     * @throws IndexOutOfBoundsException
     */
    public THIS replaceAt(int index, Component<?,?> comp) {
        if (comp == null) return _this;
        Component<?,?> otherChildComp = children.get(index);
        _add.accept(new AddedChildEvent(comp, otherChildComp, false, true));
        return _this;
    }

    /**
     * Does nothing if newComp or oldComp is null.
     *
     * @throws IndexOutOfBoundsException if oldComp does not exist in {@link #children}.
     */
    public THIS replace(Component<?,?> oldComp, Component<?,?> newComp) {
        if (oldComp == null || newComp == null) return _this;
        if (!children.contains(oldComp))
            throw new IndexOutOfBoundsException("Provided old component to be replaced does not exist in children!");
        _add.accept(new AddedChildEvent(newComp, oldComp, false, true));
        return _this;
    }

    public THIS removeSelf() {
        _removeSelf.accept(this);
        return _this;
    }

    public THIS removeAll() {
        for (Component<?,?> child : children) {
            _remove.accept(child);
        }
        return _this;
    }

    public THIS remove(Component<?,?>... comps) {
        if (comps == null) return _this;
        GodIterator.forEach(comps, _remove);
        return _this;
    }

    public THIS remove(Iterable<Component> comps) {
        if (comps == null) return _this;
        GodIterator.forEach(comps, _remove);
        return _this;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).  Returns the element that was removed from the list.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public THIS removeAt(int index) {
        Component<?,?> child = children.get(index);
        _remove.accept(child);
        return _this;
    }

    /**
     * Short for put style. <br>
     */
    public THIS sty(String key, String val) {
        _styleChange.accept(new Attribute(key, val));
        return _this;
    }

    /**
     * Short for remove style. <br>
     */
    public THIS rsty(String key) {
        _styleChange.accept(new Attribute(key, ""));
        return _this;
    }

    /**
     * Short for put attribute. <br>
     * Adds the attribute/key without its value.
     */
    public THIS atr(String key) {
        _attributeChange.accept(new AttributeChangeEvent(new Attribute(key, ""), true));
        return _this;
    }

    /**
     * Short for put attribute and value. <br>
     */
    public THIS atr(String key, String val) {
        _attributeChange.accept(new AttributeChangeEvent(new Attribute(key, val), true));
        return _this;
    }

    /**
     * Short for remove attribute. <br>
     */
    public THIS ratr(String key) {
        _attributeChange.accept(new AttributeChangeEvent(new Attribute(key, ""), false));
        return _this;
    }

    /**
     * Short for get attribute value. <br>
     * Returns the value for the provided attribute key. <br>
     * First tries to return its property value, then if that fails, tries to
     * return the value for its attribute, and returns an empty String if no key found or when value is null/undefined.
     */
    public void gatr(String key, Consumer<String> onValueReturned) {
        executeJS("try { message = comp[\"" + key + "\"]; } catch (e) { console.error(e); }\n" +
                "if(message == null) try{ message = comp.getAttribute(`" + key + "`); } catch (e) { console.error(e); }\n", onValueReturned, AL::warn);
    }

    /**
     * Must be called after changing the style or after adding/removing children to/from this component,
     * otherwise the changes won't be visible. <br>
     * If child components also changed call {@link #updateAll()} instead. <br>
     */
    public THIS update() {
        // Update element style
        StringBuilder sb = new StringBuilder();
        style.forEach((key, val) -> {
            sb.append(key).append(": ").append(val).append("; ");
        });
        element.attr("style", sb.toString());

        // Update element children list
        for (Element child : element.children()) { // Clear children
            child.remove();
        }
        for (Component<?,?> childComp : children) { // Set "new" children elements, from child components
            element.appendChild(childComp.element);
        }
        return _this;
    }

    /**
     * Performs {@link #update()} for this and all child components recursively. <br>
     * Note that you don't have to call this function, since it already gets called before showing the window,
     * in {@link UI#getSnapshot()}.
     */
    public THIS updateAll() {
        // Update this style
        update();

        // Recursion for all children
        for (Component<?,?> childComp : children) {
            childComp.updateAll();
        }

        return _this;
    }

    /**
     * Loops through all children recursively. <br>
     * Loop through {@link #children} instead if you only want the direct children of this component.
     */
    public void forEachChildRecursive(Consumer<Component<?,?>> code) {
        for (Component<?,?> child : this.children) {
            forEachChildRecursive(child, code);
        }
    }

    /**
     * Loops through all children recursively. <br>
     * Loop through {@link #children} instead if you only want the direct children of this component.
     */
    public void forEachChildRecursive(Component<?,?> comp, Consumer<Component<?,?>> code) {
        code.accept(comp);
        for (Component<?,?> child : comp.children) {
            forEachChildRecursive(child, code);
        }
    }

    /**
     * @see Element#text(String)
     */
    public THIS innerHTML(String text) {
        remove(children);
        add(new Text(text));
        return _this;
    }

    /**
     * @see Element#html(String)
     */
    public THIS innerHTML(Component<?,?> comp) {
        remove(children);
        add(comp);
        return _this;
    }

    public THIS sizeFull() {
        size("100%", "100%");
        return _this;
    }

    /**
     * Sets width and height of the target component and return it for method chaining.
     */
    public THIS size(String width, String height) {
        sty("width", width);
        sty("height", height);
        return _this;
    }

    /**
     * Sets width of the target component and return it for method chaining.
     */
    public THIS width(String s) {
        sty("width", s);
        return _this;
    }

    /**
     * Sets height of the target component and return it for method chaining.
     */
    public THIS height(String s) {
        sty("height", s);
        return _this;
    }

    public THIS padding(boolean b) {
        if (b) sty("padding", "var(--space-s)");
        else rsty("padding");
        return _this;
    }

    public THIS padding(String s) {
        sty("padding", s);
        return _this;
    }

    public THIS paddingLeft(String s) {
        sty("padding-left", s);
        return _this;
    }

    public THIS paddingRight(String s) {
        sty("padding-right", s);
        return _this;
    }

    public THIS paddingTop(String s) {
        sty("padding-top", s);
        return _this;
    }

    public THIS paddingBottom(String s) {
        sty("padding-bottom", s);
        return _this;
    }

    public THIS margin(boolean b) {
        if (b) sty("margin", "var(--space-s)");
        else rsty("margin");
        return _this;
    }

    public boolean isVisible() {
        return !style.containsKey("visibility");
    }

    public THIS visible(boolean b) {
        if (b) {
            rsty("display");
            rsty("visibility");
        } else {
            sty("display", "none");
            sty("visibility", "hidden");
        }
        return _this;
    }

    /**
     * Makes this component scrollable. <br>
     * Note that you must also set the width and height for this to work, <br>
     * and the min width and height for the child components.
     *
     * @param b if true this component will be scrollable, otherwise not.
     */
    public THIS scrollable(boolean b, String width, String height, String minChildWidth, String minChildHeight) {
        // If we want to continue using flex display
        // together with scroll, items will be shrunk to 0pixel height
        // and thus making them invisible to the user and the scroll
        // bar not appearing. The official workaround: https://stackoverflow.com/a/21541021
        // is not optimal in our case, since the child containers style will not be inherited.
        // Thus, we do the following:
        if (width != null && !width.isEmpty()) width(width);
        if (height != null && !height.isEmpty()) height(height);
        if (b) {
            if (!changedAddToSupportScroll) {
                changedAddToSupportScroll = true;
                for (Component<?,?> c : children) {
                    c.sty("min-width", minChildWidth);
                    c.sty("min-height", minChildHeight);
                }
                Consumer<AddedChildEvent> superAdd = _add;
                _add = e -> {
                    e.childComp.sty("min-width", minChildWidth);
                    e.childComp.sty("min-height", minChildHeight);
                    superAdd.accept(e);
                };
            }
            overflowAuto();
        } else {
            rsty("overflow");
        }
        return _this;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public THIS overflowDefault() {
        rsty("overflow");
        return _this;
    }

    /**
     * By default, the overflow is visible, meaning that it is not clipped and it renders outside the element's box.
     */
    public THIS overflowVisible() {
        sty("overflow", "visible");
        return _this;
    }

    /**
     * With the hidden value, the overflow is clipped, and the rest of the content is hidden.
     */
    public THIS overflowHidden() {
        sty("overflow", "hidden");
        return _this;
    }

    /**
     * Setting the value to scroll, the overflow is clipped and a scrollbar is added to scroll inside the box.
     * Note that this will add a scrollbar both horizontally and vertically (even if you do not need it).
     * Thus {@link #overflowAuto()} might be better suited.
     */
    public THIS overflowScroll() {
        sty("overflow", "scroll");
        return _this;
    }

    /**
     * The auto value is similar to scroll, but it adds scrollbars only when necessary.
     */
    public THIS overflowAuto() {
        sty("overflow", "auto");
        return _this;
    }

    /**
     * By default, flex items are laid out in the source order. <br>
     * However, the order property controls the order in which they appear in the flex container. <br>
     * Items with the same order revert to source order. <br>
     * Default: 0 <br>
     */
    public THIS order(int i) {
        sty("order", String.valueOf(i));
        return _this;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public THIS orderDefault() {
        rsty("order");
        return _this;
    }

    /**
     * This defines the ability for a flex item to grow if necessary.
     * It accepts a unitless value that serves as a proportion.
     * It dictates what amount of the available space inside the flex container the item should take up.
     * <p>
     * If all items have flex-grow set to 1, the remaining space
     * in the container will be distributed equally to all children.
     * If one of the children has a value of 2, that child would take up twice
     * as much of the space either one of the others (or it will try, at least). <br>
     * <p>
     * Negative numbers are invalid. <br>
     * Default: 0 <br>
     */
    public THIS grow(int i) {
        sty("flex-grow", String.valueOf(i));
        return _this;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public THIS growDefault() {
        rsty("flex-grow");
        return _this;
    }

    /**
     * This defines the ability for a flex item to shrink if necessary. <br>
     * Negative numbers are invalid. <br>
     * Default: 1 <br>
     */
    public THIS shrink(int i) {
        sty("flex-shrink", String.valueOf(i));
        return _this;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public THIS shrinkDefault() {
        rsty("flex-shrink");
        return _this;
    }

    /**
     * By default, flex items will all try to fit onto one line.
     * You can change that and allow the items to wrap as needed with this property. <br>
     * false/nowrap (default): all flex items will be on one line <br>
     * true/wrap: flex items will wrap onto multiple lines, from top to bottom. <br>
     */
    public THIS wrap(boolean b) {
        if (b) sty("flex-wrap", "wrap");
        else sty("flex-wrap", "nowrap");
        return _this;
    }

    /**
     * justify-self <br>
     * The element is positioned at the beginning of the container. <br>
     */
    public THIS selfStart1() {
        sty("justify-self", "flex-start");
        return _this;
    }

    /**
     * The element is positioned at the end of the container.
     *
     * @see #selfStart1()
     */
    public THIS selfEnd1() {
        sty("justify-self", "flex-end");
        return _this;
    }

    /**
     * The element is positioned at the center of the container.
     *
     * @see #selfStart1()
     */
    public THIS selfCenter1() {
        sty("justify-self", "center");
        return _this;
    }

    /**
     * Default. The element inherits its parent container's align-items property,
     * or "stretch" if it has no parent container.
     *
     * @see #selfStart1()
     */
    public THIS selfAuto1() {
        sty("justify-self", "auto");
        return _this;
    }

    /**
     * The element is positioned to fit the container.
     *
     * @see #selfStart1()
     */
    public THIS selfStretch1() {
        sty("justify-self", "stretch");
        return _this;
    }


    /**
     * align-self <br>
     * The element is positioned at the beginning of the container. <br>
     * This allows the default alignment (or the one specified by align-items)
     * to be overridden for individual flex items. <br>
     * Note that float, clear and vertical-align have no effect on a flex item.
     */
    public THIS selfStart2() {
        sty("align-self", "flex-start");
        return _this;
    }

    /**
     * The element is positioned at the end of the container.
     *
     * @see #selfStart2()
     */
    public THIS selfEnd2() {
        sty("align-self", "flex-end");
        return _this;
    }

    /**
     * The element is positioned at the center of the container.
     *
     * @see #selfStart2()
     */
    public THIS selfCenter2() {
        sty("align-self", "center");
        return _this;
    }

    /**
     * Default. The element inherits its parent container's align-items property,
     * or "stretch" if it has no parent container.
     *
     * @see #selfStart2()
     */
    public THIS selfAuto2() {
        sty("align-self", "auto");
        return _this;
    }

    /**
     * The element is positioned to fit the container.
     *
     * @see #selfStart2()
     */
    public THIS selfStretch2() {
        sty("align-self", "stretch");
        return _this;
    }

    /**
     * Aligns items top to bottom.
     */
    public THIS childVertical() {
        sty("flex-direction", "column");
        return _this;
    }

    /**
     * (Default) Aligns items left to right in ltr; right to left in rtl.
     */
    public THIS childHorizontal() {
        sty("flex-direction", "row");
        return _this;
    }

    /**
     * Vertical child layout. <br>
     * Creates, adds and returns a new child layout with vertical component alignment.
     */
    public Vertical verticalCL() {
        Vertical layout = new Vertical().padding(false);
        add(layout);
        return layout;
    }

    /**
     * Horizontal child layout. <br>
     * Creates, adds and returns a new child layout with horizontal component alignment.
     */
    public Horizontal horizontalCL() {
        Horizontal layout = new Horizontal().padding(false);
        add(layout);
        return layout;
    }

    /**
     * Smart child layout. <br>
     * Creates, adds and returns a new smart child layout.
     */
    public SmartLayout smartCL() {
        SmartLayout layout = new SmartLayout().padding(false);
        add(layout);
        return layout;
    }

    public THIS childStart(){
        childStart1().childStart2();
        return _this;
    }

    /**
     * justify-content (along primary axis) <br>
     * flex-start (default): items are packed toward the start of the flex-direction.
     */
    public THIS childStart1() {
        sty("justify-content", "flex-start");
        return _this;
    }

    public THIS childEnd(){
        childEnd1().childEnd2();
        return _this;
    }

    /**
     * justify-content (along primary axis) <br>
     * flex-end: items are packed toward the end of the flex-direction.
     */
    public THIS childEnd1() {
        sty("justify-content", "flex-end");
        return _this;
    }

    public THIS childCenter(){
        childCenter1().childCenter2();
        return _this;
    }

    /**
     * justify-content (along primary axis) <br>
     * center: items are centered along the line
     */
    public THIS childCenter1() {
        sty("justify-content", "center");
        return _this;
    }

    /**
     * justify-content (along primary axis) <br>
     * space-between: items are evenly distributed in the line; first item is on the start line, last item on the end line
     */
    public THIS childSpaceBetween1() {
        sty("justify-content", "space-between");
        return _this;
    }

    /**
     * justify-content (along primary axis) <br>
     * space-around: items are evenly distributed in the line with equal space around them.
     * Note that visually the spaces aren't equal, since all the items
     * have equal space on both sides. The first item will have one unit of space
     * against the container edge, but two units of space between
     * the next item because that next item has its own spacing that applies.
     */
    public THIS childSpaceAround1() {
        sty("justify-content", "space-around");
        return _this;
    }

    /**
     * justify-content (along primary axis) <br>
     * space-evenly: items are distributed so that the spacing
     * between any two items (and the space to the edges) is equal.
     */
    public THIS childSpaceEvenly1() {
        sty("justify-content", "space-around");
        return _this;
    }

    /**
     * align-items (along secondary axis) <br>
     * flex-start (default): items are packed toward the start of the flex-direction.
     */
    public THIS childStart2() {
        sty("align-items", "flex-start");
        return _this;
    }

    /**
     * align-items (along secondary axis) <br>
     * flex-end: items are packed toward the end of the flex-direction.
     */
    public THIS childEnd2() {
        sty("align-items", "flex-end");
        return _this;
    }

    /**
     * align-items (along secondary axis) <br>
     * center: items are centered along the line
     */
    public THIS childCenter2() {
        sty("align-items", "center");
        return _this;
    }

    /**
     * align-items (along secondary axis) <br>
     * stretch: stretch to fill the container (still respect min-width/max-width)
     */
    public THIS childStretch2() {
        sty("align-items", "stretch");
        return _this;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges.
     */
    public THIS childGap(boolean b) {
        if (b) sty("gap", "var(--space-s)");
        else rsty("gap");
        return _this;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges.
     */
    public THIS childGap(String s) {
        sty("gap", s);
        return _this;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges. <br>
     * Gap between rows/Y-Axis/height.
     */
    public THIS childGapY(String s) {
        sty("row-gap", s);
        return _this;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges. <br>
     * Gap between columns/X-Axis/width.
     */
    public THIS childGapX(String s) {
        sty("column-gap", s);
        return _this;
    }

    /**
     * Adds a CSS class to this component.
     */
    public THIS addClass(String s) {
        String classes = element.attr("class");
        classes += " " + s;
        atr("class", classes);
        return _this;
    }

    /**
     * Removes a CSS class from this component.
     */
    public THIS removeClass(String s) {
        String classes = element.attr("class");
        classes = classes.replace(s, "");
        atr("class", classes);
        return _this;
    }

    public THIS removeAllClasses() {
        atr("class", "");
        return _this;
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, Consumer)
     */
    public THIS onClick(Consumer<ClickEvent<THIS>> code) {
        readOnlyOnClick.addAction((event) -> code.accept(event));
        Component<THIS, VALUE> _this = this;
        UI.get().registerJSListener("click", _this, (msg) -> {
            readOnlyOnClick.execute(new ClickEvent<THIS>(msg, (THIS) _this)); // Executes all listeners
        });
        return this._this;
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was double-clicked by the user. <br>
     *
     * @see UI#registerJSListener(String, Component, Consumer)
     */
    public THIS onDoubleClick(Consumer<ClickEvent<THIS>> code) {
        AtomicLong msLastClick = new AtomicLong();
        onClick(e -> {
            long msCurrentClick = System.currentTimeMillis();
            long msBetweenClicks = msCurrentClick - msLastClick.get();
            if(msBetweenClicks < 500){
                code.accept(e);
            }
            msLastClick.set(msCurrentClick);
        });
        return this._this;
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, Consumer)
     */
    public THIS onScroll(Consumer<ScrollEvent<THIS>> code) {
        readOnlyOnScroll.addAction((event) -> code.accept(event));
        Component<THIS, VALUE> _this = this;
        UI.get().registerJSListener("scroll", _this,
                "message = `{\"isReachedEnd\": \"` + (Math.abs(event.target.scrollHeight - event.target.scrollTop - event.target.clientHeight) < 1) + `\"," +
                        " \"scrollHeight\": \"` + event.target.scrollHeight + `\"," +
                        " \"scrollTop\": \"` + event.target.scrollTop + `\"," +
                        " \"clientHeight\": \"` + event.target.clientHeight + `\"," +
                        " \"eventAsJson\":` + message + `}`;\n",
                (msg) -> {
                    readOnlyOnScroll.execute(new ScrollEvent<THIS>(msg, (THIS) _this)); // Executes all listeners
                });
        return this._this;
    }

    public Component<?,?> firstChild() {
        return children.get(0);
    }

    public Component<?,?> lastChild() {
        return children.get(children.size() - 1);
    }

    public String toPrintString(){
        return this.getClass().getSimpleName()+"_"+id;
    }

    public @Nullable Tooltip tooltip = null;

    public THIS setTooltip(String content){
        this.tooltip = new Tooltip(this, content);
        tooltip.attachToParent();
        return _this;
    }

    public THIS setTooltip(Tooltip tooltip){
        this.tooltip = tooltip;
        tooltip.parent = this;
        tooltip.attachToParent();
        return _this;
    }

    /**
     * Enables or disables the component.
     *
     * @param b true to enable, false to disable
     * @return the current instance of Component
     */
    public THIS enable(boolean b) {
        if (b) ratr("disabled");
        else atr("disabled");
        return _this;
    }

    /**
     * Scrolls the component into view with default options (smooth animation, start block, nearest inline).
     *
     * @return the current instance of Component
     */
    public THIS scrollIntoView(){
        return scrollIntoView(true, "start", "nearest");
    }

    /**
     * Scrolls the component into view with customizable options.
     *
     * @param smooth if true: scrolling should animate smoothly, otherwise instant
     * @param block defines vertical alignment (start, center, end, nearest)
     * @param inline defines horizontal alignment (start, center, end, nearest)
     * @return the current instance of Component
     */
    public THIS scrollIntoView(boolean smooth, String block, String inline){
        executeJS("comp.scrollIntoView({ behavior: \"" +
                (smooth ? "smooth" : "instant") +
                "\", block: \"" +
                block +
                "\", inline: \"" +
                inline +
                "\" });");
        return _this;
    }

    /**
     * Scrolls the component to a specified position.
     *
     * @param x the horizontal position
     * @param y the vertical position
     * @return the current instance of Component
     */
    public THIS scrollTo(int x, int y) {
        executeJS("comp.scrollTo(" + x + ", " + y + ");");
        return _this;
    }

    /**
     * Scrolls the component by a specified amount.
     *
     * @param x the horizontal distance to scroll
     * @param y the vertical distance to scroll
     * @return the current instance of Component
     */
    public THIS scrollBy(int x, int y) {
        executeJS("comp.scrollBy(" + x + ", " + y + ");");
        return _this;
    }

    /**
     * Scrolls the component to the top.
     *
     * @return the current instance of Component
     */
    public THIS scrollToTop() {
        executeJS("comp.scrollTop = 0;");
        return _this;
    }

    /**
     * Scrolls the component to the bottom.
     *
     * @return the current instance of Component
     */
    public THIS scrollToBottom() {
        executeJS("comp.scrollTop = comp.scrollHeight;");
        return _this;
    }

    /**
     * Scrolls the component to the top with smooth animation.
     *
     * @param duration the duration of the animation in milliseconds
     * @return the current instance of Component
     */
    public THIS scrollToTopSmooth(int duration) {
        executeJS("let start = comp.scrollTop; let startTime = null; function scrollTo(timestamp) { if (!startTime) startTime = timestamp; let elapsed = timestamp - startTime; let progress = elapsed / duration; comp.scrollTop = start * (1 - Math.pow(2, -10 * progress)); if (elapsed < duration) { window.requestAnimationFrame(scrollTo); } } window.requestAnimationFrame(scrollTo);");
        return _this;
    }

    /**
     * Scrolls the component to the bottom with smooth animation.
     *
     * @param duration the duration of the animation in milliseconds
     * @return the current instance of Component
     */
    public THIS scrollToBottomSmooth(int duration) {
        executeJS("let start = comp.scrollTop; let startTime = null; let scrollHeight = comp.scrollHeight - comp.clientHeight; function scrollTo(timestamp) { if (!startTime) startTime = timestamp; let elapsed = timestamp - startTime; let progress = elapsed / duration; comp.scrollTop = scrollHeight * Math.pow(2, 10 * (progress - 1)); if (elapsed < duration) { window.requestAnimationFrame(scrollTo); } } window.requestAnimationFrame(scrollTo);");
        return _this;
    }

    /**
     * Scrolls the component to the left with smooth animation.
     *
     * @param duration the duration of the animation in milliseconds
     * @return the current instance of Component
     */
    public THIS scrollToLeftSmooth(int duration) {
        executeJS("let start = comp.scrollLeft; let startTime = null; function scrollTo(timestamp) { if (!startTime) startTime = timestamp; let elapsed = timestamp - startTime; let progress = elapsed / duration; comp.scrollLeft = start * (1 - Math.pow(2, -10 * progress)); if (elapsed < duration) { window.requestAnimationFrame(scrollTo); } } window.requestAnimationFrame(scrollTo);");
        return _this;
    }

    /**
     * Scrolls the component to the right with smooth animation.
     *
     * @param duration the duration of the animation in milliseconds
     * @return the current instance of Component
     */
    public THIS scrollToRightSmooth(int duration) {
        executeJS("let start = comp.scrollLeft; let startTime = null; let scrollWidth = comp.scrollWidth - comp.clientWidth; function scrollTo(timestamp) { if (!startTime) startTime = timestamp; let elapsed = timestamp - startTime; let progress = elapsed / duration; comp.scrollLeft = scrollWidth * Math.pow(2, 10 * (progress - 1)); if (elapsed < duration) { window.requestAnimationFrame(scrollTo); } } window.requestAnimationFrame(scrollTo);");
        return _this;
    }

    /**
     * Sets the z-index for the component using the specified enum value.
     *
     * @param zIndexEnum the enum value representing the desired z-index
     * @return the current instance of Component
     */
    public THIS setZIndex(ZIndex zIndexEnum) {
        sty("z-index", ""+zIndexEnum.value);
        return _this;
    }

    /**
     * @param percent for example 0.75 would be 75% of the original size.
     *                Meaning 0 to 1 is equal to 0% to 100%, you can also go above 100%.
     */
    public THIS scale(double percent){
        sty("zoom",  ""+percent);
        sty("-moz-transform", "scale("+percent+")");
        sty("-moz-transform-origin", "0 0");
        return _this;
    }

    /**
     * Based on https://getbootstrap.com/docs/5.3/layout/z-index/
     */
    public enum ZIndex {
        DROPDOWN(1000),
        STICKY(1020),
        FIXED(1030),
        OFFCANVAS_BACKDROP(1040),
        OFFCANVAS(1045),
        MODAL_BACKDROP(1050),
        MODAL(1055),
        POPOVER(1070),
        TOOLTIP(1080),
        TOAST(1090);

        private final int value;

        ZIndex(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    //
    // Listeners
    //
    public static class AttributeChangeEvent {
        public final Attribute attribute;
        public final boolean isInsert;

        public AttributeChangeEvent(Attribute attribute, boolean isInsert) {
            this.attribute = attribute;
            this.isInsert = isInsert;
        }
    }

    public static class AddedChildEvent {
        /**
         * Component that got added.
         */
        public Component<?,?> childComp;
        /**
         * Should only be relevant and not null when either
         * {@link #isInsert} or {@link #isReplace} is true.
         */
        public Component<?,?> otherChildComp;
        /**
         * If true, then {@link #otherChildComp} should now be the next child in the list after {@link #childComp}.
         */
        public boolean isInsert;
        /**
         * If true, then {@link #otherChildComp} should NOT exist in the list anymore.
         */
        public boolean isReplace;
        /**
         * True if this is the first time {@link #childComp} was added to another component.
         */
        public boolean isFirstAdd;

        public AddedChildEvent(Component<?,?> childComp, Component<?,?> otherChildComp, boolean isInsert, boolean isReplace) {
            this.childComp = childComp;
            this.isFirstAdd = childComp.isFirstAdd;
            childComp.isFirstAdd = false;
            this.otherChildComp = otherChildComp;
            this.isInsert = isInsert;
            this.isReplace = isReplace;
        }
    }
}
