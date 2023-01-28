# Desku
Java Framework for developing Desktop Applications with Java, HTML and CSS.
[Click here for Maven/Gradle/Sbt/Leinigen instructions](https://jitpack.io/#Osiris-Team/Desku/LATEST) (Java 8 or higher required).

```java
import com.osiris.desku.swing.NativeWindow;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.IOException;

public class HelloWorldApp {
    public static void main(String[] args) throws IOException {
        // Setup app details
        App.name = "My-App";

        // Create routes
        Route home = new Route("/"){
            @Override
            public Node loadContent() {
                return new TextNode("Hello World!");
            }
        };

        // Create windows
        new NativeWindow(home);
    }
}
```
![img.png](img.png)

### Features
- Latest Chromium HTML/CSS renderer via [JCEF](https://github.com/jcefmaven/jcefbuild).
- Runs on Windows/Linux/Mac by installing platform-specific dependencies at first launch.
- Minimal memory and cpu usage since no additional JavaScript engine (Node.js) is being used.
- Localhost-free and server-less.
