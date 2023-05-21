# Desku [![](https://jitpack.io/v/Osiris-Team/Desku.svg)](https://jitpack.io/#Osiris-Team/Desku)
Java Framework for developing Desktop and Mobile Applications in one codebase with Java/JS, HTML and CSS.
[Click here for Maven/Gradle/Sbt/Leinigen instructions](https://jitpack.io/#Osiris-Team/Desku/LATEST) (Java 8 or higher required).

<p align="center">
  <img src="https://github.com/Osiris-Team/Desku/blob/main/docs/img.png?raw=true" alt=""/>
</p>

```java
public class Main {
    public static void main(String[] args) throws IOException, UnsupportedPlatformException, CefInitializationException, InterruptedException {
        // Setup app details
        App.init(new DesktopUIManager(false));
        App.name = "My-App";

        // Create routes
        Route home = new MRoute("/", () -> { // It's recommended to create a new class and extend Route instead (for larger UIs)
            return new Layout().add(new Text("Hello World!")); 
        });

        // Create and show windows
        new DesktopUI(home);
    }
}
```

### Features
#### Its highly recommended to use the [Desku-Gradle-Starter-App](https://github.com/Osiris-Team/Desku-Gradle-Starter-App) to get started since it has also support for Android and iOS, and everything setup correctly.
All features/components are tested [here](https://github.com/Osiris-Team/Desku/tree/main/src/test/java/com/osiris/desku/simple_app).

- Easily develop desktop and mobile apps in one codebase!
- Minimal memory and cpu usage since no additional JavaScript engine (Node.js) is being used.
- Each UIs content is provided by a tiny HTTP server and
Java <=> JavaScript interactions are handled by a even tinier WebSocket server.
- Full Java [FlexBox](https://css-tricks.com/snippets/css/a-guide-to-flexbox/) 
bindings, thus making simple/complex layout creation faster and easier than ever.
- You decide! Code your UI in Java or directly in HTML/CSS, or both!
- Focus on method-chaining and low-code.
- Update the UI asynchronously hassle-free.

### Extensions
A list of all available extensions can be found [here](https://github.com/topics/desku-extension?o=desc&s=updated).
It can be a single component or a complete suite of multiple components, either
way its pretty easy to create a Desku-Extension:
1. Add the Desku dependency.
2. Extend the Component class and start coding (example component [here](https://github.com/Osiris-Team/Desku/blob/main/src/test/java/com/osiris/desku/VerticalLayout.java)).
3. Publish your repo on GitHub with the #desku-extension tag/topic (also mention the Desku version your extension supports / was built with).
4. Create a release and use JitPack or Maven to host the assets.

### Todo
- Serializable UI.
- Navigation between UIs/windows.
- Default components suit similar to https://vaadin.com/docs/latest/components.
- JavaFX WebView instead of JCEF to reduce startup time and disk size of app?

### Documentation

<div>
<details>
<summary>App.getCSS/getJS methods return null? Resources cannot be found?</summary>

By default, build tools will remove anything that is not a .java source file,
thus your .css/.js or any other files will not be included in the final binary.
Here is how you can fix this in Gradle, simply append the below to your `build.gradle` file:
```groovy
// Ensure that everything other than classes/.java files are also included in the final jar
// This should also be included in your project if you want to easily load resources.
sourceSets {
    main {
        resources {
            srcDirs = ["src/main/java", "src/main/resources"]
            include '**/*' // Include everything (no .java by default)
        }

    }
}
// This must also be included if you want to generate the sources jar without issues
tasks.withType(Jar).configureEach { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
```
</details>


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

<details>
<summary>How do I implement my own UI and UIManager? Why would I?</summary>

UI and UIManager are both abstract classes that can be extended.
Desku already provides implementations (DesktopUI and DesktopUIManager)
via JCEF to be able to run on Desktop platforms like Windows, Linux and Mac.

The Desku-Starter-App contains implementations for Android and iOS. If you
want to support even more platforms make a pull-request with your implementation!

</details>


</div>
