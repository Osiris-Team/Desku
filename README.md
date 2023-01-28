# Desku
Java Framework for developing Desktop Applications with Java, HTML and CSS.
[Click here for Maven/Gradle/Sbt/Leinigen instructions](https://jitpack.io/#Osiris-Team/Desku/LATEST) (Java 8 or higher required).

```java
public class Main {
    public static void main(String[] args) throws IOException {
        // Setup app details
        App.name = "My-App";

        // Create routes
        Route home = new Route("/"){
            @Override
            public Component<?> loadContent() {
                return new Layout().text("Hello World!");
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
- Full Java [FlexBox](https://css-tricks.com/snippets/css/a-guide-to-flexbox/) 
implementation, thus making simple/complex layout creation faster and easier than ever.
