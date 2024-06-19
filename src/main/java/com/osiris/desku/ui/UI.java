package com.osiris.desku.ui;

import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.ui.utils.Rectangle;
import com.osiris.events.Event;
import com.osiris.jlib.logger.AL;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Native window with HTML content that is generated by provided {@link Route}. <br>
 * SHOULD NOT MAKE USE OF ANY JAVA APIs THAT ARE SPECIFIC TO A PLATFORM LIKE AWT <br>
 * TO ENSURE COMPATIBILITY ACROSS PLATFORMS.
 */
public abstract class UI {
    private static final Map<Thread, UI> threadsAndUIs = new HashMap<>();
    public final AtomicBoolean isLoading = new AtomicBoolean(true);
    /**
     * Relevant when wanting HTML load state, since we cant run JavaScript before the page is fully loaded.<br>
     * Boolean parameter isLoading, is true if still loading or false if finished loading.
     */
    public final Event<Boolean> onLoadStateChanged = new Event<>();
    /**
     * Last loaded html.
     */
    public Route route;
    public Component<?,?> content;
    /**
     * Not thread safe, access inside synchronized block.
     */
    public HashMap<String, List<Component<?,?>>> listenersAndComps = new HashMap<>();
    public WSServer webSocketServer = null;
    public HTTPServer httpServer;

    public UI(Route route) throws Exception {
        this(route, false, true, 70, 60);
    }

    public UI(Route route, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception {
        startHTTPServer();
        startWebSocketServer();

        //load(route.getClass()); // Done in HTTPServer

        safeInit("http://" + App.domainName + ":" + httpServer.serverPort + (route.path.startsWith("/") ? "" : "/") + route.path,
                isTransparent, isDecorated, widthPercent, heightPercent);

    }

    //
    // Abstract methods that require implementation
    //

    /**
     * Initialises/Displays the window and loads the HTML from the provided startURL.
     *
     * @param startURL      URL of the HTML content. Example: http://localhost or https://google.com or file:///ABSOLUTE_PATH_TO_HTML_FILE
     * @param isTransparent
     * @param isDecorated
     * @param widthPercent
     * @param heightPercent
     */
    public abstract void init(String startURL, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception;

    /**
     * This invalidates the container and thus to see changes in the UI
     * make sure execute {@link java.awt.Component#revalidate()} manually.
     *
     * @param widthPercent 0 to 100% of the parent size (screen if null).
     */
    public abstract void width(int widthPercent) throws InterruptedException, InvocationTargetException;

    /**
     * This invalidates the container and thus to see changes in the UI
     * make sure execute {@link java.awt.Component#revalidate()} manually.
     *
     * @param heightPercent 0 to 100% of the parent size (screen if null).
     */
    public abstract void height(int heightPercent) throws InterruptedException, InvocationTargetException;

    /**
     * Moves the window on the X axis.
     *
     * @param x amount to add to current x value.
     */
    public abstract void plusX(int x) throws InterruptedException, InvocationTargetException;

    /**
     * Moves the window on the Y axis.
     *
     * @param y amount to add to current y value.
     */
    public abstract void plusY(int y) throws InterruptedException, InvocationTargetException;

    /**
     * Executes JavaScript code now. Method may wait until execution finishes, or not.
     * However, it must ensure that the code is executed in an orderly, synchronous fashion.
     * Meaning that the JavaScript code in the second call of this method, gets executed after the code in the first call.
     * @param jsCode JavaScript (JS) code.
     * @param jsCodeSourceName file name containing the JS code.
     * @param jsCodeStartingLineNumber line number/position the JS code starts in.
     */
    public abstract void executeJavaScript(String jsCode, String jsCodeSourceName, int jsCodeStartingLineNumber);

    /**
     * Maximizes this window if true, otherwise restores the previous state.
     */
    public abstract void maximize(boolean b) throws InterruptedException, InvocationTargetException;

    /**
     * Minimizes this window if true, otherwise restores the previous state.
     */
    public abstract void minimize(boolean b) throws InterruptedException, InvocationTargetException;

    /**
     * Puts this window into full-screen if true, otherwise restores the previous state.
     */
    public abstract void fullscreen(boolean b) throws InterruptedException, InvocationTargetException;

    /**
     * Adds a listener which gets executed when this windows size changes.
     */
    public abstract void onSizeChange(Consumer<Rectangle> code) throws InterruptedException, InvocationTargetException;

    public abstract Rectangle getScreenSize() throws InterruptedException, InvocationTargetException;

    public abstract Rectangle getScreenSizeWithoutTaskBar() throws InterruptedException, InvocationTargetException;

    /**
     * If true decorates the window, otherwise removes the decoration.
     */
    public abstract void decorate(boolean b) throws InterruptedException, InvocationTargetException;

    /**
     * If true sets this window to be on top of all other windows, otherwise restores the previous state.
     */
    public abstract void allwaysOnTop(boolean b) throws InterruptedException, InvocationTargetException;

    /**
     * If true focuses this window, otherwise loses focus.
     */
    public abstract void focus(boolean b) throws InterruptedException, InvocationTargetException;

    /**
     * Changes the windows background color. <br>
     * Also changes the background color of {@link #content} if not null. <br>
     * @param hexColor example: "#FF0000FF" (first 2 digits are red, then green, then blue, then alpha/opacity where 00 is transparent and FF fully visible).
     */
    public abstract void background(String hexColor) throws InterruptedException, InvocationTargetException;

    //
    // Utility methods
    //

    /**
     * Always null, except when code is running inside... <br>
     * - {@link #access(Runnable)} <br>
     * - constructor {@link #UI(Route, boolean, boolean, int, int)} when generating this UIs' HTML  for the first time. <br>
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

    private final List<PendingAppend> pendingAppends = new ArrayList<>();

    /**
     * Executes JavaScript to navigate to another route. <br>
     * Note that {@link #z_internal_load(Class)} will be called by {@link #startWebSocketServer()}
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

    public void reload(){
        executeJavaScript("window.location.reload();", "internal", 0);
    }

    /**
     * Access this window synchronously now.
     */
    public UI access(Runnable code) {
        UI.set(this, Thread.currentThread());
        code.run();
        UI.remove(Thread.currentThread());

        synchronized (pendingAppends){
            for (PendingAppend pendingAppend : pendingAppends) {
                try{
                    attachToParentSafely(pendingAppend);
                } catch (Exception e) {
                    AL.warn(e);
                }
            }
            pendingAppends.clear();
        }
        return this;
    }

    public void safeInit(String startURL, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) {
        try {
            AL.info("Starting new UI/window with url: " + startURL + " transparent: " + isTransparent + " width: " + widthPercent + "% height: " + heightPercent + "%");
            AL.info("Waiting for it to finish loading... Please stand by...");
            long ms = System.currentTimeMillis();

            AtomicBoolean isLoaded = new AtomicBoolean(false);
            /**
             * {@link #startWebSocketServer()}
             */
            onLoadStateChanged.addAction((action, isLoading) -> {
                if (!isLoading) {
                    action.remove();
                    isLoaded.set(true);
                }
            }, Exception::printStackTrace);

            App.uis.all.add(this);
            init(startURL, isTransparent, isDecorated, widthPercent, heightPercent);

            while (!isLoaded.get()) Thread.yield();

            AL.info("Init took " + (System.currentTimeMillis() - ms) + "ms for " + this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        App.uis.all.remove(this);
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
            Document html = route.getBaseDocument();
            // Append styles
            Element elGlobalCSSLink = new Element("link");
            elGlobalCSSLink.attr("rel", "stylesheet");
            elGlobalCSSLink.attr("href", App.styles.getName());
            html.getElementsByTag("head").get(0).appendChild(elGlobalCSSLink);

            // Append actual content
            Element outlet = html.getElementById("outlet");
            content.updateAll();
            outlet.appendChild(content.element);

            // Execute JS at the end, after all HTML was loaded
            Element elJSConnectToBackendWS = new Element("script");
            elJSConnectToBackendWS.html(
                    "function onPageLoaded(callback) {\n" +
                            "  async function notifyOnPageLoad() {\n" +
                            "    if (document.readyState === 'complete') {\n" +
                            //"      console.log('Page finished loading.');\n" +
                            "      callback();\n" +
                            "    } else {\n" +
                            "      setTimeout(notifyOnPageLoad, 100); // 100ms\n" +
                            "    }\n" +
                            "  }\n" +
                            "\n" +
                            //"  console.log('Waiting for page to finish loading...');\n" +
                            "  notifyOnPageLoad(); // Perform the initial check immediately\n" +
                            "}\n\n" +
                    startWebSocketClient(webSocketServer.domain, webSocketServer.port));
            html.getElementsByTag("body").get(0).appendChild(elJSConnectToBackendWS);

            Element elGlobalJSLink = new Element("script");
            elGlobalJSLink.attr("src", App.javascript.getName());
            html.getElementsByTag("body").get(0).appendChild(elGlobalJSLink);
            return html;
        } else {
            content.updateAll();
            Element html = content.element;
            while ((html = html.parent()) != null) ;
            return (Document) html;
        }
    }

    public String startWebSocketClient(String serverDomain, int serverPort) {
        String url = "ws://" + serverDomain + ":" + serverPort;
        String jsCode = "try{    var webSocketServer = new WebSocket('" + url + "');\n" +
                "window.webSocketServer = webSocketServer;\n" + // Make globally accessible
                "console.log(\"Connecting to WebSocket server...\")\n"+
                "\n" +
                "    webSocketServer.addEventListener(\"open\", (event) => {\n" +
                "      console.log('WebSocket connection established.');\n" +
                "    });\n" +
                "\n" +
                "    webSocketServer.addEventListener(\"message\", (event) => {\n" +
                "      // Receive a message from the server\n" +
                "      var receivedMessage = event.data;\n" +
                "      console.log('Received message from server:', receivedMessage);\n" +
                "    });\n" +
                "\n" +
                "    webSocketServer.addEventListener(\"close\", (event) => {\n" +
                "      console.log('WebSocket connection closed.');\n" +
                "    });\n" +
                "} catch (e) {console.error(e)}\n";
        return jsCode;
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
    public void widthFull() throws InterruptedException, InvocationTargetException {
        width(100);
    }

    /**
     * @see #height(int)
     */
    public void heightFull() throws InterruptedException, InvocationTargetException {
        height(100);
    }

    /**
     * Returns new JS (JavaScript) code, that when executed in client browser
     * results in onJSFunctionExecuted being executed. <br><br>
     *
     * It wraps around your jsCode and adds callback related stuff, as well as error handling. <br><br>
     *
     * Its a permanent callback, because the returned JS code can be executed multiple times
     * which results in onJSFunctionExecuted or onJSFuntionFailed to get executed multiple times too. <br><br>
     *
     * Also appends a check to the JS code that sets message="" if it was null/undefined.<br><br>
     *
     * @param jsCode               modify the message variable in the provided JS (JavaScript) code to send information from JS to Java.
     *                             Your JS code could look like this: <br>
     *                             message = 'first second third etc...';
     * @param onSuccess executed when the provided jsCode executes successfully. String contains the message variable that can be set in your jsCode.
     * @param onError   executed when the provided jsCode threw an exception. String contains details about the exception/error.
     */
    public String jsAddPermanentCallback(String jsCode, Consumer<String> onSuccess, Consumer<String> onError) {
        // 1. execute client JS
        // 2. execute callback in java with params from client JS
        // 3. return success to client JS and execute it
        int id = webSocketServer.counter.getAndIncrement();
        synchronized (webSocketServer.javaScriptCallbacks) {
            PendingJavaScriptResult pendingJavaScriptResult = new PendingJavaScriptResult(id, onSuccess, onError);
            webSocketServer.javaScriptCallbacks.add(pendingJavaScriptResult);
        }
        return "var message = '';\n" + // Separated by space
                "var error = null;\n" +
                "try{" + jsCode + "\n" +
                "if(message==null) message = '';\n" +
                "} catch (e) { error = e; console.error(e);}\n" +
                jsClientSendWebSocketMessage("(error == null ? ('" + id + " '+message) : ('!" + id + " '+error))");
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
        this.content.forEachChildRecursive(child -> {
            child.setAttached(true);
        });
        this.content.setAttached(true);
        onLoadStateChanged.addAction((isLoading) -> {
            if (isLoading) return;
            this.isLoading.set(false);
        });
        UI.remove(Thread.currentThread());
    }

    public UI registerDocJSListener(String eventName, String jsOnEvent, Consumer<String> onEvent,
                                 boolean waitUntilLoaded) {
        return registerJSListener(eventName, null, jsOnEvent, onEvent, waitUntilLoaded);
    }

    /**
     * @see #registerJSListener(String, Component, String, Consumer, boolean)
     */
    public UI registerJSListener(String eventName, Component comp, Consumer<String> onEvent) {
        return registerJSListener(eventName, comp, "", onEvent, true);
    }

    public UI registerJSListener(String eventName, Component comp, String jsOnEvent, Consumer<String> onEvent) {
        return registerJSListener(eventName, comp, jsOnEvent, onEvent, true);
    }

    /**
     * Registers this listener directly only if the page was loaded,
     * otherwise adds an action to {@link #onLoadStateChanged} to register the listener later.
     *
     * @param eventName name of the JavaScript event to listen for.
     * @param comp      component to register the listener on. If null, document will be used as component.
     * @param jsOnEvent additional JavaScript code that is run when the event is triggered and has access
     *                  to the variables:  <br>
     *                  message: which is the string that is returned to Java and contains the event as json object. <br>
     *                  event: which is the event object. <br>
     * @param onEvent   executed when event happened. Has {@link #access(Runnable)}.
     */
    public UI registerJSListener(String eventName, Component comp, String jsOnEvent, Consumer<String> onEvent,
                                 boolean waitUntilLoaded) {
        synchronized (listenersAndComps) {
            List<Component<?,?>> alreadyRegisteredComps = listenersAndComps.get(eventName);
            if (alreadyRegisteredComps == null) {
                alreadyRegisteredComps = new ArrayList<>();
                listenersAndComps.put(eventName, alreadyRegisteredComps);
            }
            if (alreadyRegisteredComps.contains(comp))
                return this; // Already registered
            alreadyRegisteredComps.add(comp);
        }
        String jsNow =
                "comp.addEventListener(\"" + eventName + "\", (event) => {\n" +
                jsAddPermanentCallback("function getObjProps(obj) {\n" +
                                "  var json = '{';\n" +
                                "  for (const key in obj) {\n" +
                                "    if (obj[key] !== obj && obj[key] !== null && obj[key] !== undefined) {\n" +
                                "      if(key == 'data') continue;\n" + // Skip data since it's the same as value
                                "      json += (`\"${key}\": \"${obj[key]}\",`);\n" + // Also hope that values do not contain JSON breaking chars
                                "    }\n" +
                                "  }\n" +
                                "  if(json[json.length-1] == ',') json = json.slice(0, json.length-1);" + // Remove last ,
                                "  json += '}';\n" +
                                "  return json;\n" +
                                "}" +
                                "message = getObjProps(event)\n" +
                                jsOnEvent,
                        (message) -> {
                            App.executor.execute(() -> { // async
                                access(() -> {
                                    try {
                                        onEvent.accept(message); // Should execute all listeners
                                    } catch (Exception e) {
                                        AL.warn(e);
                                    }
                                });
                            });
                        },
                        (error) -> {
                            throw new RuntimeException(error);
                        }) + // JS code that triggers Java function gets executed on a click event for this component
                "});\n";

        if(!waitUntilLoaded){
            if (comp == null) executeJavaScript("let comp = document\n"+jsNow, "internal", 0);
            else comp.executeJS(this, jsNow, false);
        }
        else if (!isLoading.get()) {
            if (comp == null) executeJavaScript("let comp = document\n"+jsNow, "internal", 0);
            else comp.executeJS(this, jsNow, true);
        }
        else onLoadStateChanged.addAction((action, isLoading) -> {
            if (isLoading) return;
            action.remove();
                if (comp == null) executeJavaScript("let comp = document\n"+jsNow, "internal", 0);
                else comp.executeJS(this, jsNow, true);
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
        webSocketServer = new WSServer(serverDomain, serverPort){
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                super.onOpen(conn, handshake);
                // Executed when client connects, since its executed at the end of the HTML body
                // this tells us that the page is loaded for the first time too
                AL.info(this+" init success!");
                onLoadStateChanged.execute(false);
            }
        };
        serverPort = webSocketServer.port;
        AL.info("Started WebSocketServer " + serverDomain + ":" + serverPort + " for UI: " + this);
    }

    public String jsClientSendWebSocketMessage(String message) {
        return "if(webSocketServer.readyState != 1) {\n" +
                "console.log(`Frontend and Backend are connected!`)\n" + // 1 == OPEN
                "  webSocketServer.addEventListener(\"open\", (event) => {\n" +
                "      webSocketServer.send(" + message + ");\n"+
                "  });\n" +
                "} else webSocketServer.send(" + message + ");\n";
    }

    public boolean isOpen() {
        return httpServer != null && httpServer.server.isAlive();
    }

    public String jsGetComp(String varName, int id) {
        return "var " + varName + " = document.querySelector('[java-id=\"" + id + "\"]');\n";
    }

    /**
     * Ensures all parents are attached before performing actually
     * performing the pending append operation.
     */
    private void attachToParentSafely(PendingAppend pendingAppend) {
        if(pendingAppend.child.isAttached()) return;

        List<MyElement> parents = new ArrayList<>();
        MyElement parent = pendingAppend.parent.element;
        while(parent != null && parent instanceof MyElement){
            parents.add(parent); // Make sure that the last attached parent is given too
            if(parent.comp.isAttached()) break;
            Element p = parent.parent();
            if(p instanceof MyElement) parent = (MyElement) p;
            else break;
        }
        if(parents.size() >= 2){
            MyElement rootParentParent = parents.get(parents.size() - 1); // attached
            MyElement rootParent = parents.get(parents.size() - 2); // not attached yet
            // If this gets appended, there is no need of
            // performing the pending append operations of all its children.
            rootParent.comp.updateAll();
            attachToParent(rootParentParent.comp, rootParent.comp,
                    new Component.AddedChildEvent(rootParent.comp, null, false, false));
            // Above also sets isAttached = true of all child components recursively,
            // thus next attachToParentSafely() will return directly without doing nothing,
            // and thus all pending appends for those children will not be executed,
            // since otherwise that would cause duplicate components
        } else{
            pendingAppend.child.updateAll();
            attachToParent(pendingAppend.parent, pendingAppend.child, pendingAppend.e);
        }
    }

    public void attachWhenAccessEnds(Component<?,?> parent, Component<?,?> child, Component.AddedChildEvent e) {
        synchronized (pendingAppends){
            pendingAppends.add(new PendingAppend(parent, child, e));
        }
    }

    public <T extends Component<?,?>> void attachToParent(Component<?,?> parent, Component<?,?> child, Component.AddedChildEvent e) {
        //AL.info("attachToParent() "+parent.getClass().getSimpleName()+"("+parent.id+"/"+parent.isAttached()+") ++++ "+
        //        child.getClass().getSimpleName()+"("+child.id+") ");

        if (e.otherChildComp == null) { // add
            executeJavaScript(jsAttachToParent(parent, child),
                    "internal", 0);
            child.setAttached(true);
            child.forEachChildRecursive(child2 -> {
                child2.setAttached(true);
            });
        } else if (e.isInsert || e.isReplace) { // for replace, remove() must be executed after this function returns
            executeJavaScript(
                            jsGetComp("otherChildComp", e.otherChildComp.id) +
                            "var child = `" + e.childComp.element.outerHtml() + "`;\n" +
                            "otherChildComp.insertAdjacentHTML(\"beforebegin\", child);\n",
                    "internal", 0);
            e.childComp.setAttached(true);
            e.childComp.forEachChildRecursive(child2 -> {
                child2.setAttached(true);
            });
        }
    }

    public String jsAttachToParent(Component<?,?> parent, Component<?,?> child) {
        return "try{"+jsGetComp("parentComp", parent.id) +
                        "var child = `\n" + child.element.outerHtml() + "\n`;\n" +
                        "parentComp.insertAdjacentHTML(\"beforeend\", child);\n" +
                        //"console.log('ADDED CHILD: ');\n"+
                        "console.log(child);\n}catch(e){console.error(e)}";
    }

    private class PendingAppend{
        public Component<?,?> parent;
        public Component<?,?> child;
        public Component.AddedChildEvent e;

        public PendingAppend(Component<?,?> parent, Component<?,?> child, Component.AddedChildEvent e) {
            this.parent = parent;
            this.child = child;
            this.e = e;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PendingAppend that = (PendingAppend) o;

            if (!Objects.equals(parent, that.parent)) return false;
            return Objects.equals(child, that.child);
        }

        @Override
        public int hashCode() {
            int result = parent != null ? parent.hashCode() : 0;
            result = 31 * result + (child != null ? child.hashCode() : 0);
            return result;
        }
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
