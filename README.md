# Desku [![](https://jitpack.io/v/Osiris-Team/Desku.svg)](https://jitpack.io/#Osiris-Team/Desku)
Java Framework for developing Desktop Applications with Java, HTML and CSS.
[Click here for Maven/Gradle/Sbt/Leinigen instructions](https://jitpack.io/#Osiris-Team/Desku/LATEST) (Java 8 or higher required).

**Note that support for mobile apps (Android/iOS) is planned and being worked on.**

<p align="center">
  <img src="https://github.com/Osiris-Team/Desku/blob/main/docs/img.png?raw=true" alt=""/>
</p>

```java
public class Main {
    public static void main(String[] args) throws IOException {
        // Setup app details
        App.name = "My-App";

        // Create routes
        Route home = new MRoute("/", () -> { // It's recommended to create a new class and extend Route instead (for larger UIs)
            return new Layout().add(new Text("Hello World!")); 
        });

        // Create windows
        new NativeWindow(home);
    }
}
```

A more complete example can be found at the [Desku-Gradle-Starter-App](https://github.com/Osiris-Team/Desku-Gradle-Starter-App)
repo.

### Features
- Latest Chromium HTML/CSS renderer via [JCEF](https://github.com/jcefmaven/jcefbuild).
- Runs on Windows/Linux/Mac by installing platform-specific dependencies at first launch.
- Minimal memory and cpu usage since no additional JavaScript engine (Node.js) is being used.
- Localhost-free and server-less.
- Full Java [FlexBox](https://css-tricks.com/snippets/css/a-guide-to-flexbox/) 
implementation, thus making simple/complex layout creation faster and easier than ever.
- Focus on method-chaining and low-code.

### Extensions
A list of all available extensions can be found [here](https://github.com/topics/desku-extension?o=desc&s=updated).
It can be a single component or a complete suite of multiple components, either
way its pretty easy to create a Desku-Extension:
1. Add the Desku dependency.
2. Extend the Component class and start coding (example component [here](https://github.com/Osiris-Team/Desku/blob/main/src/test/java/com/osiris/desku/VerticalLayout.java)).
3. Publish your repo on GitHub with the #desku-extension tag/topic.
4. Create a release and use JitPack or Maven to host the assets.

### Todo
- Serializable UI
- Default components suit similar to https://vaadin.com/docs/latest/components

### Documentation

<div>
<details>
<summary>How to change the theme?</summary>

The theme can be changed quite easily by setting
the `App.theme` variable. <br>
Create your own themes by extending the `Theme` class
where you modify existing attributes or add new ones
and update the `App.theme` variable.
</details>



<details>
<summary>How do I add my own JavaScript event listener?</summary>

Probably the best and easiest way to show is with an example.
The code below shows the JavaScript click event being implemented:
```java
public class ClickEvent extends JavaScriptEvent {
    public final boolean isTrusted;
    public final int screenX, screenY;

    public ClickEvent(String rawJSMessage, Component<?> comp) {
        super(rawJSMessage, comp);
        this.isTrusted = jsMessage.get("isTrusted").getAsBoolean();
        this.screenX = jsMessage.get("screenX").getAsInt();
        this.screenY = jsMessage.get("screenY").getAsInt();
    }
}

public class MyComp extends Component<MyComp>{
    /**
     * Do not add actions via this variable, use {@link #onClick(Consumer)} instead.
     */
    public final Event<ClickEvent> _onClick = new Event<>();
    public MyComp(){
        init(this, "my-comp");
    }
    /**
     * Adds a listener that gets executed when this component was clicked.
     */
    public MyComp onClick(Consumer<ClickEvent> code) {
        _onClick.addAction((event) -> code.accept(event));
        Component<?> _this = this;
        UI.current().registerJSListener("click", _this, (msg) -> {
            _onClick.execute(new ClickEvent(msg, _this)); // Executes all listeners
        });
        return target;
    }
};
```
You can register listeners on any JavaScript event 
you'd like: https://developer.mozilla.org/en-US/docs/Web/Events
</details>



<details>
<summary>How to get the HTML of a component?</summary>

Get the components' HTML string via 
`component.element.outerHTML()`. <br>
Note that this also includes all its children.
To make sure it equals the actual in memory representation
call `component.updateAll()` before retrieving the HTML.
</details>


</div>
