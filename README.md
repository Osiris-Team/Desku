# Desku [![](https://jitpack.io/v/Osiris-Team/Desku.svg)](https://jitpack.io/#Osiris-Team/Desku)
Java Framework for developing low-code Desktop and Mobile Applications in one codebase with Java/JS, HTML and CSS.
[Click here for Maven/Gradle/Sbt/Leinigen instructions](https://jitpack.io/#Osiris-Team/Desku/LATEST) (Java 11 or higher required).

### Desku is in early development, thus critical bugs and incomplete features are to be expected.

### Who is it for?
Mainly backend developers that want to code their frontend/GUI directly in Java in a low-code, fast and pain-less way.
In addition, it is also highly beginner-friendly, making it accessible to everyone that is new to coding due to its simplicity.

### What does it look like?
```java
import com.osiris.desku.App;
import static com.osiris.desku.Statics; // Low-code Java UI via static methods

public class Main {
    public static void main(String[] args) throws Exception {
        // Setup app details
        App.init(new DesktopUIManager());
        App.name = "My-App";

        // Create routes
        // For larger UIs create a new class and extend Route instead
        Route home = new MRoute("/", () -> { 
            return vertical().add(text("Hello World!")); // Low-code Java UI via static methods
        });

        // Create and show windows
        App.uis.create(home);
    }
}
```
<details>
<summary>Screenshots</summary>

AppTest home page (24.08.2023), which includes all default components:
![img.png](/docs/AppTestPage.png)
</details>

### How to get started?
#### Use the [Desku-Gradle-Starter-App](https://github.com/Osiris-Team/Desku-Gradle-Starter-App) as starting point since it also supports Android and iOS, everything is setup correctly and scripts for generating binaries + installers are included.
#### Usage examples for all default components can be found [here](https://github.com/Osiris-Team/Desku/blob/main/src/test/java/com/osiris/desku/simple_app/home/Home.java) (CTRL + F to search by name).
#### Install the [Desku-Intellij-Plugin](https://github.com/Osiris-Team/Desku-Intellij-Plugin) to make development even more hassle-free and generate the little boilerplate there is.

### License
Desku is released under a modified MIT license (see full license for details):
> If your project generates more than 1000€ a month in revenue and uses this software, you are required to pay a royalty fee to Osiris Team.
> The royalty fee is 5% of gross revenue (i.e., the total revenue generated by your project).
> You must make a monthly payment to osiris_support@pm.me over PayPal.
> Other ways of payment are possible, contact the above email for further information.

### Features
- Easily develop desktop and mobile apps in one codebase!
- Full Java [FlexBox](https://css-tricks.com/snippets/css/a-guide-to-flexbox/) 
bindings, thus making simple/complex layout creation faster and easier than ever.
- You decide! Code your UI in Java or directly in HTML/CSS, or both!
- Focus on method-chaining and low-code (check out the Statics class).
- Update the UI asynchronously hassle-free.
- Contains cross-platform desktop WebView implementation already. 
Android and iOS implementations are provided in the starter repo.
- Uses [Bootstrap v5.3.0](https://getbootstrap.com/docs/5.3/components) for styling and [Jsoup](https://jsoup.org/) for handling the HTML of components.

### Extensions
A list of all available extensions can be found [here](https://github.com/topics/desku-extension?o=desc&s=updated).
It can be a single component or a complete suite of multiple components, either
way, its pretty easy to create a Desku-Extension:
1. Start with the [Desku-Gradle-Starter-Extension](https://github.com/Osiris-Team/Desku-Gradle-Starter-Extension) template.
2. Publish your repo on GitHub with the #desku-extension tag/topic (also mention the Desku version your extension supports / was built with).
3. Create a release and use JitPack or Maven to host the files.

### Todo
- Native notifications and in-app overlay notifications.
- Default components suit similar to https://vaadin.com/docs/latest/components.
- Serializable UI, to restore state (on ice right now, since it seems
to be more complicated than expected)

### Contributing
Contributions are welcome! Especially HTML5 component integrations, aka
porting an HTML5 component to a Java Desku component.

When building remember to include this specific test, to also update
the `Statics` class.
```
./gradlew build :test --tests "com.osiris.desku.GenerateStatics"
```


### Documentation

![](https://github.com/Osiris-Team/Desku/blob/main/docs/ChatGPT%20Support%20Banner.png?raw=true)
Copy and send [this](https://raw.githubusercontent.com/Osiris-Team/Desku/main/README.md) 
to [ChatGPT](https://chat.openai.com/) and ask it questions to get quick support.

#### User interface

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

public class MyComp extends Component<MyComp> {
    /**
     * Do not add actions via this variable, use {@link #onClick(Consumer)} instead.
     */
    public final Event<ClickEvent> _onClick = new Event<>();

    public MyComp() {
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
}
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
<summary>What about performance?</summary>

- Minimal memory and cpu usage since no additional JavaScript engine (Node.js) is being used.
- Each UIs content is provided by a tiny HTTP server and
Java <=> JavaScript interactions are handled by an even tinier WebSocket server.
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




<details>
<summary>Logging? Log cleanup?</summary>

For logging, you can use the `AL` class and its static methods info/debug, warn and error.
These are pre-formatted with ANSI colors, info is white, debug dark gray,
warn yellow and error red. Colors are stripped when writing to files and the formatting is slightly different.

You can pass Exceptions to warn and error. The stacktrace (plus all the causes) will then
be displayed. Note that warn only shows the Exceptions' message in the console.
The full stacktrace can only be seen in the log file, by default.

When using error your app will exit in the next 10 seconds, thus you should
use it only if the occurred Exception is critical and hinders your app from
running.

Note that debug will only be shown in the log file by default, not the console.

This is part of the [jlib](https://github.com/Osiris-Team/jlib) library,
which contains some more useful things you might want to check out.

TODO: Remove older logs to save space on the users' device.
</details>


</div>




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
<summary>How to support even more platforms? Custom UI and UIManager?</summary>

UI and UIManager are both abstract classes that can be extended.
Desku already provides implementations (DesktopUI and DesktopUIManager)
via WebView to be able to run on Desktop platforms like Windows, Linux and Mac.

The Desku-Starter-App contains implementations for Android and iOS. If you
want to support even more platforms make a pull-request with your implementation!

</details>

  
  
<p align="center">
  <img src="https://github.com/Osiris-Team/Desku/blob/main/docs/desku_banner.png?raw=true" alt=""/>
</p>
