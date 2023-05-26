# Desku [![](https://jitpack.io/v/Osiris-Team/Desku.svg)](https://jitpack.io/#Osiris-Team/Desku)
Java Framework for developing low-code Desktop and Mobile Applications in one codebase with Java/JS, HTML and CSS.
[Click here for Maven/Gradle/Sbt/Leinigen instructions](https://jitpack.io/#Osiris-Team/Desku/LATEST) (Java 8 or higher required).

<p align="center">
  <img src="https://github.com/Osiris-Team/Desku/blob/main/docs/desku_banner.png?raw=true" alt=""/>
</p>

```java
import static com.osiris.desku.Statics; // Low-code Java UI via static methods

public class Main {
    public static void main(String[] args) throws IOException, UnsupportedPlatformException, CefInitializationException, InterruptedException {
        // Setup app details
        App.init(new DesktopUIManager(false));
        App.name = "My-App";

        // Create routes
        Route home = new MRoute("/", () -> { // It's recommended to create a new class and extend Route instead (for larger UIs)
            return layout().add(text("Hello World!")); // Low-code Java UI via static methods
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
Java <=> JavaScript interactions are handled by an even tinier WebSocket server.
- Full Java [FlexBox](https://css-tricks.com/snippets/css/a-guide-to-flexbox/) 
bindings, thus making simple/complex layout creation faster and easier than ever.
- You decide! Code your UI in Java or directly in HTML/CSS, or both!
- Focus on method-chaining and low-code (check out the Statics class).
- Update the UI asynchronously hassle-free.

### Extensions
A list of all available extensions can be found [here](https://github.com/topics/desku-extension?o=desc&s=updated).
It can be a single component or a complete suite of multiple components, either
way, its pretty easy to create a Desku-Extension:
1. Start with the [Desku-Gradle-Starter-Extension](https://github.com/Osiris-Team/Desku-Gradle-Starter-Extension) template.
2. Publish your repo on GitHub with the #desku-extension tag/topic (also mention the Desku version your extension supports / was built with).
3. Create a release and use JitPack or Maven to host the files.

### Todo
- Serializable UI.
- Navigation between UIs/windows.
- Default components suit similar to https://vaadin.com/docs/latest/components.
- JavaFX WebView instead of JCEF to reduce startup time and disk size of the app?

### Contributing
Contributions are welcome! Especially HTML5 component integrations, aka
porting an HTML5 component to a Java Desku component.

When building remember to include this specific test, to also update
the `Statics` class.
```
./gradlew build :test --tests "com.osiris.desku.GenerateStatics"
```


### Documentation

#### Frequently asked

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
        super("my-comp");
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

#### Extensions

<div>
<details>
<summary>Extending another component? Method chaining not possible / fallback to super class. What to do?</summary>

The problem in more detail:
```java
public class A extends Component<A>{
    // ...
    public A methodInA(){
        return this;
    }
}

public class B extends A{
    // ...
    public B methodInB(){
        return this;
    }
}

public class Main{
  public void main(){
    new B().methodInA(); // If we want to do method chaining, aka access
    // another method of class B, its not possible anymore
    // due to Java language limits, since now the returned value is of type A.
  }
}
```
Instead of extending classes we are forced (if we want to provide method chaining)
to add the super class as field of our current class and wrap around important methods, like so:

```java
public class B extends Component<B>{ // Instead of extending A
    public final A a = new A(); 
    public B(){
        super("b");
        add(a); // Add as child
    }
    // ...
    public B methodInA(){
        a.methodInA();
        return this;
    }
}
```

</details>

</div>

#### Other

<div>
<details>
<summary>Persistent storage/data? Databases/SQL?</summary>

I find it easiest to use [jSQL-Gen](https://github.com/Osiris-Team/jSQL-Gen)
(also developed by me),
which generates the Java source code that is needed to interact
with your database and solve this issue in a low-code fashion.
Note that your database can be integrated in your app / exist on the client directly
(via [mariaBD4J](https://github.com/MariaDB4j/MariaDB4j) for example)
or hosted by yourself on your server.

(TODO) If you want to store data in the local storage of the clients' browser/webview,
you can use ui.localStorage which 
is the Java implementation of [localStorage](https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage).
Note that the data here is specific to a window/UI/Route, which means that its not shared
across them.

</details>

</div>
