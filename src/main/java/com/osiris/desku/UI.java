package com.osiris.desku;

import com.osiris.desku.ui.Component;
import com.osiris.events.Event;
import com.osiris.jlib.logger.AL;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Native window with HTML content that is generated by provided {@link Route}.
 */
public abstract class UI {
    private static final Map<Thread, UI> threadsAndUIs = new HashMap<>();
    public final AtomicBoolean isLoading = new AtomicBoolean(true);
    /**
     * Relevant when wanting HTML load state, since we cant run JavaScript before the page is fully loaded.
     */
    public final Event<Boolean> onLoadStateChanged = new Event<>();
    /**
     * Last loaded html.
     */
    public Route route;
    public Component<?> content;
    /**
     * Not thread safe, access inside synchronized block.
     */
    public HashMap<String, List<Component<?>>> listenersAndComps = new HashMap<>();
    public WSServer webSocketServer = null;
    public HTTPServer httpServer;

    public UI(Route route) throws Exception {
        this(route, false, 70, 60);
    }

    public UI(Route route, boolean isTransparent, int widthPercent, int heightPercent) throws Exception {
        startHTTPServer();
        startWebSocketServer();

        //load(route.getClass()); // Done in HTTPServer

        priv_init("http://" + App.domainName + ":" + httpServer.serverPort + (route.path.startsWith("/") ? "" : "/") + route.path, isTransparent, widthPercent, heightPercent);
        startWebSocketClient(webSocketServer.domain, webSocketServer.port);
    }

    /**
     * Always null, except when code is running inside... <br>
     * - {@link #access(Runnable)} <br>
     * - constructor {@link #UI(Route, boolean, int, int)} when generating this UIs' HTML  for the first time. <br>
     * - any triggered JavaScript event that was registered via {@link #registerJSListener(String, Component, Consumer)}. <br>
     * or {@link #set(UI, Thread...)} was called before.
     */
    public static UI get() {
        // Current code is not inside access(), thus we check the thread
        synchronized (threadsAndUIs) {
            return threadsAndUIs.get(Thread.currentThread());
        }
    }

    /**
     * Maps the provided threads to the provided UI, so that when calling
     * {@link UI#get()} inside those threads it returns the provided UI.
     */
    public static void set(UI ui, Thread... threads) {
        synchronized (threadsAndUIs) {
            for (Thread t : threads) {
                threadsAndUIs.put(t, ui);
            }
        }
    }

    public static void remove(Thread... threads) {
        synchronized (threadsAndUIs) {
            for (Thread t : threads) {
                threadsAndUIs.remove(t);
            }
        }
    }

    /**
     * Access this window synchronously now.
     */
    public UI access(Runnable code) {
        UI.set(this, Thread.currentThread());
        code.run();
        UI.remove(Thread.currentThread());
        return this;
    }

    /**
     * Initialises/Displays the window and loads the HTML from the provided startURL.
     *
     * @param startURL      URL of the HTML content. Example: http://localhost or https://google.com or file:///ABSOLUTE_PATH_TO_HTML_FILE
     * @param isTransparent
     * @param widthPercent
     * @param heightPercent
     */
    public abstract void init(String startURL, boolean isTransparent, int widthPercent, int heightPercent) throws Exception;

    /**
     * Executes JavaScript to navigate to another route. <br>
     * Note that {@link #z_internal_load(Class)} will be called by {@link #httpServer}
     * and thus the content of this UI modified.
     */
    public void navigate(Class<? extends Route> routeClass) {
        Route route = null;
        for (Route r : App.routes) {
            if (r.getClass().equals(routeClass)) {
                route = r;
                break;
            }
        }
        if (route == null) { // Route was not registered
            AL.warn("Failed to navigate to page, since provided route '" + routeClass
                    + "' was not registered, aka not added to App.routes!", new Exception());
            return;
        }
        executeJavaScript("window.location.href = `" + route.path + "`;", "internal", 0);
    }

    /**
     * Note that this method is meant to be used internally. <br>
     * Use {@link #navigate(Class)} instead if you want to send the
     * user to another page.
     */
    public void z_internal_load(Class<? extends Route> routeClass) throws IOException {
        Route route = null;
        for (Route r : App.routes) {
            if (r.getClass().equals(routeClass)) {
                route = r;
                break;
            }
        }
        if (route == null) { // Route was not registered
            AL.warn("Failed to load page, since provided route '" + routeClass
                    + "' was not registered, aka not added to App.routes!", new Exception());
            return;
        }
        UI.set(this, Thread.currentThread());
        this.isLoading.set(true);
        this.route = route;
        this.listenersAndComps.clear();
        this.content = route.loadContent();
        onLoadStateChanged.addAction((isLoading) -> {
            if (isLoading) return;
            this.isLoading.set(false);
        });
        UI.remove(Thread.currentThread());
    }

    private void priv_init(String startURL, boolean isTransparent, int widthPercent, int heightPercent) {
        try {
            AL.info("Starting new window with url: " + startURL + " transparent: " + isTransparent + " width: " + widthPercent + "% height: " + heightPercent + "%");
            AL.info("Please stand by...");
            UIManager.all.add(this);
            long ms = System.currentTimeMillis();
            init(startURL, isTransparent, widthPercent, heightPercent);
            AL.info("Init took " + (System.currentTimeMillis() - ms) + "ms for " + this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        UIManager.all.remove(this);
        try {
            webSocketServer.stop();
            AL.info("Closed WebSocketServer " + webSocketServer.domain + ":" + webSocketServer.port + " for UI: " + this);
        } catch (Exception e) {
        }
        try {
            httpServer.server.stop();
            AL.info("Closed HTTPServer " + httpServer.serverDomain + ":" + httpServer.serverPort + " for UI: " + this);
        } catch (Exception e) {
        }
        AL.info("Closed " + this);
    }

    /**
     * Creates a snapshot of the current UI (the full HTML page) <br>
     * and returns it as {@link Document} for further processing. <br>
     * Note that changes to it won't be reflected in the actual UI.
     */
    public Document getSnapshot() {
        if (content.element.parent() == null) {
            // First load
            Document html = route.getDocument();
            // Append styles
            Element elGlobalCSSLink = new Element("link");
            elGlobalCSSLink.attr("rel", "stylesheet");
            elGlobalCSSLink.attr("href", App.styles.getName());
            html.getElementsByTag("head").get(0).appendChild(elGlobalCSSLink);

            Element elGlobalJSLink = new Element("script");
            elGlobalJSLink.attr("src", App.javascript.getName());
            html.getElementsByTag("head").get(0).appendChild(elGlobalJSLink);

            // Append actual content
            Element outlet = html.getElementById("outlet");
            content.updateAll();
            outlet.appendChild(content.element);
            return html;
        } else {
            content.updateAll();
            Element html = content.element;
            while ((html = html.parent()) != null) ;
            return (Document) html;
        }
    }

    /**
     * @see #snapshotToTempFile(Document)
     */
    public File snapshotToTempFile() throws IOException {
        return snapshotToTempFile(null);
    }

    /**
     * Creates a snapshot and writes the HTML to a file in the temp folder of the current user. <br>
     * If the path for this route is "/persons/john" for example then the html file will be created at: <br>
     * {@link App#tempDir}/{@link #hashCode()}/persons/john.html <br>
     * Note that the hash code is in hex format.
     *
     * @return the generated html file.
     */
    public File snapshotToTempFile(Document snapshot) throws IOException {
        File file = getSnapshotTempFile();
        if (snapshot == null) snapshot = getSnapshot();

        // Write html to temp file
        AL.info("Generate: " + file);
        file.getParentFile().mkdirs();
        if (!file.exists()) file.createNewFile();
        Files.write(file.toPath(), snapshot.outerHtml().getBytes(StandardCharsets.UTF_8));
        return file;
    }

    public File getSnapshotTempFile() {
        return new File(App.htmlDir
                + (route.path.equals("/") || route.path.equals("") ? "/.html" : (route.path + ".html")));
    }

    /**
     * @see #width(int)
     */
    public void widthFull() {
        width(100);
    }

    /**
     * This invalidates the container and thus to see changes in the UI
     * make sure execute {@link java.awt.Component#revalidate()} manually.
     *
     * @param widthPercent 0 to 100% of the parent size (screen if null).
     */
    public abstract void width(int widthPercent);

    /**
     * @see #height(int)
     */
    public void heightFull() {
        height(100);
    }

    /**
     * This invalidates the container and thus to see changes in the UI
     * make sure execute {@link java.awt.Component#revalidate()} manually.
     *
     * @param heightPercent 0 to 100% of the parent size (screen if null).
     */
    public abstract void height(int heightPercent);

    /**
     * Moves the window on the X axis.
     *
     * @param x amount to add to current x value.
     */
    public abstract void plusX(int x);

    /**
     * Moves the window on the Y axis.
     *
     * @param y amount to add to current y value.
     */
    public abstract void plusY(int y);

    public abstract void executeJavaScript(String jsCode, String jsCodeSourceName, int jsCodeStartingLineNumber);

    /**
     * Returns new JS (JavaScript) code, that when executed in client browser
     * results in onJSFunctionExecuted being executed. <br>
     * It wraps around your jsCode and adds callback related stuff, as well as error handling. <br>
     * Its a permanent callback, because the returned JS code can be executed multiple times
     * which results in onJSFunctionExecuted or onJSFuntionFailed to get executed multiple times too. <br>
     *
     * @param jsCode               modify the message variable in the provided JS (JavaScript) code to send information from JS to Java.
     *                             Your JS code could look like this: <br>
     *                             message = 'first second third etc...';
     * @param onJSFunctionExecuted executed when the provided jsCode executes successfully. String contains the message variable that can be set in your jsCode.
     * @param onJSFunctionFailed   executed when the provided jsCode threw an exception. String contains details about the exception/error.
     */
    public String jsAddPermanentCallback(String jsCode, Consumer<String> onJSFunctionExecuted, Consumer<String> onJSFunctionFailed) {
        // 1. execute js code
        // 2. execute callback in java with params from js code
        // 3. return success to js code and execute it
        int id = webSocketServer.counter.getAndIncrement();
        synchronized (webSocketServer.javaScriptCallbacks) {
            PendingJavaScriptResult pendingJavaScriptResult = new PendingJavaScriptResult(id, onJSFunctionExecuted, onJSFunctionFailed);
            webSocketServer.javaScriptCallbacks.add(pendingJavaScriptResult);
        }
        return "var message = '';\n" + // Separated by space
                "var error = null;\n" +
                "try{" + jsCode + "} catch (e) { error = e; }\n" +
                jsClientSendWebSocketMessage("(error == null ? ('" + id + " '+message) : ('!" + id + " '+error))");
    }

    public String jsGetComp(String varName, int id) {
        return "var " + varName + " = document.querySelectorAll('[java-id=\"" + id + "\"]')[0];\n";
    }

    /**
     * @see #registerJSListener(String, Component, String, Consumer)
     */
    public <T extends Component> UI registerJSListener(String eventName, Component<T> comp, Consumer<String> onEvent) {
        return registerJSListener(eventName, comp, "", onEvent);
    }

    /**
     * Registers this listener directly only if the page was loaded,
     * otherwise adds an action to {@link #onLoadStateChanged} to register the listener later.
     *
     * @param eventName name of the JavaScript event to listen for.
     * @param comp      component to register the listener on.
     * @param jsOnEvent additional JavaScript code that is run when the event is triggered and has access
     *                  to the variables:  <br>
     *                  message: which is the string that is returned to Java and contains the event as json object. <br>
     *                  event: which is the event object. <br>
     * @param onEvent   executed when event happened. Has {@link #access(Runnable)}.
     */
    public <T extends Component> UI registerJSListener(String eventName, Component<T> comp, String jsOnEvent, Consumer<String> onEvent) {
        synchronized (listenersAndComps) {
            List<Component<?>> alreadyRegisteredComps = listenersAndComps.get(eventName);
            if (alreadyRegisteredComps == null) {
                alreadyRegisteredComps = new ArrayList<>();
                listenersAndComps.put(eventName, alreadyRegisteredComps);
            }
            if (alreadyRegisteredComps.contains(comp))
                return this; // Already registered
            alreadyRegisteredComps.add(comp);
        }
        String jsNow = jsGetComp("comp", comp.id) +
                "comp.addEventListener(\"" + eventName + "\", (event) => {\n" +
                jsAddPermanentCallback("function getObjProps(obj) {\n" +
                                "  var json = '{';\n" +
                                "  for (const key in obj) {\n" +
                                "    if (obj[key] !== obj && obj[key] !== null && obj[key] !== undefined) {\n" +
                                "      json += (`\"${key}\": \"${obj[key]}\",`);\n" +
                                "    }\n" +
                                "  }\n" +
                                "  if(json[json.length-1] == ',') json = json.slice(0, json.length-1);" + // Remove last ,
                                "  json += '}';\n" +
                                "  return json;\n" +
                                "}" +
                                "message = getObjProps(event)\n" +
                                jsOnEvent,
                        (message) -> {
                            access(() -> {
                                try {
                                    onEvent.accept(message); // Should execute all listeners
                                } catch (Exception e) {
                                    AL.warn(e);
                                }
                            });
                        },
                        (error) -> {
                            throw new RuntimeException(error);
                        }) + // JS code that triggers Java function gets executed on a click event for this component
                "});\n";

        if (!isLoading.get()) executeJavaScript(jsNow, "internal", 0);
        else onLoadStateChanged.addAction((action, isLoading) -> {
            if (isLoading) return;
            action.remove();
            executeJavaScript(jsNow, "internal", 0);
        }, AL::warn);
        return this;
    }

    public void startHTTPServer() throws Exception {
        int freePort = App.httpServerPort;
        if (freePort == -1)
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                // Set the port to 0 to let the system allocate a free port
                freePort = serverSocket.getLocalPort();
            }
        startHTTPServer(App.domainName, freePort);
    }

    public synchronized void startHTTPServer(String serverDomain, int serverPort) throws Exception {
        httpServer = new HTTPServer(this, serverDomain, serverPort);
        serverPort = httpServer.serverPort;
        AL.info("Started HTTPServer " + serverDomain + ":" + serverPort + " for UI: " + this);
    }

    /**
     * Uses {@link App#domainName} and searches for a random free port
     * (if {@link App#webSocketServerPort} is -1) to use for the WebSocketServer.
     */
    public void startWebSocketServer() throws Exception {
        int freePort = App.webSocketServerPort;
        if (freePort == -1)
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                // Set the port to 0 to let the system allocate a free port
                freePort = serverSocket.getLocalPort();
            }
        startWebSocketServer(App.domainName, freePort);
    }

    /**
     * Returns JS code that when executed on the client browser creates a WebSocket connection.
     * Also creates and inits the WebSocketServer if needed.
     *
     * @param serverDomain
     * @param serverPort
     * @return
     */
    public synchronized void startWebSocketServer(String serverDomain, int serverPort) throws Exception {
        webSocketServer = new WSServer(serverDomain, serverPort);
        serverPort = webSocketServer.port;
        AL.info("Started WebSocketServer " + serverDomain + ":" + serverPort + " for UI: " + this);
    }

    public void startWebSocketClient(String serverDomain, int serverPort) {
        String url = "ws://" + serverDomain + ":" + serverPort;
        String jsCode = "    var webSocketServer = new WebSocket('" + url + "');\n" +
                "window.webSocketServer = webSocketServer;\n" + // Make globally accessible
                "\n" +
                "    webSocketServer.onOpen = function(event) {\n" +
                "      console.log('WebSocket connection established.');\n" +
                "\n" +
                "      // Send a message to the server\n" +
                "      var message = 'Hello from the client!';\n" +
                "      socket.send(message);\n" +
                "    };\n" +
                "\n" +
                "    webSocketServer.onMessage = function(event) {\n" +
                "      // Receive a message from the server\n" +
                "      var receivedMessage = event.data;\n" +
                "      console.log('Received message from server:', receivedMessage);\n" +
                "    };\n" +
                "\n" +
                "    webSocketServer.onClose = function(event) {\n" +
                "      console.log('WebSocket connection closed.');\n" +
                "    };";
        executeJavaScript(jsCode, "internal", 0);
    }

    public String jsClientSendWebSocketMessage(String message) {
        return "webSocketServer.send(" + message + ");\n";
    }

    public class PendingJavaScriptResult {
        public final int id;
        public final Consumer<String> onSuccess;
        public final Consumer<String> onError;
        public boolean isPermanent = true;

        public PendingJavaScriptResult(int id, Consumer<String> onSuccess, Consumer<String> onError) {
            this.id = id;
            this.onSuccess = onSuccess;
            this.onError = onError;
        }
    }
}
