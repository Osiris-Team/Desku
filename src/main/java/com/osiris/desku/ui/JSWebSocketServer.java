package com.osiris.desku.ui;

import com.osiris.desku.App;
import com.osiris.desku.ui.utils.Event;
import com.osiris.jlib.logger.AL;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * A WebSocket server that sends JavaScript code to be executed in the browser
 * and returns its execution result.
 */
public class JSWebSocketServer extends WebSocketServer {
    public final String domain;
    public final int port;
    public final List<PendingJavaScriptResult> javaScriptCallbacks = new CopyOnWriteArrayList<>();
    public final AtomicInteger counter = new AtomicInteger();
    public transient long lastPingReceivedMs = System.currentTimeMillis();
    public final Thread timeoutCheckerThread = new Thread(() -> {
        // Kind of wasteful to have a thread pretty much always in sleep, virtual threads would be perfect here
        // TODO however not sure if we lose a lot of backwards support of android versions if we upgrade the java version
        int maxSeconds = 30;
        long maxMillis = Duration.ofSeconds(maxSeconds).toMillis();
        var toRemove = new ArrayList<PendingJavaScriptResult>(0);
        while(true){
            try{
                Thread.sleep(1000);
                long now = System.currentTimeMillis();
                synchronized (javaScriptCallbacks){
                    toRemove.clear();
                    for (PendingJavaScriptResult javaScriptCallback : javaScriptCallbacks) {
                        if(javaScriptCallback.isPermanent) continue; // Skip permanent callbacks
                        if(now - javaScriptCallback.msCreated > maxMillis){
                            System.err.println("This pending javascript result never got a response from the browser/client within "+maxSeconds+" seconds,\n" +
                                    "thus it was removed and will never complete which might result in unexpected behavior.\n" +
                                    "This could mean the client closed the page during something running or something else went entirely wrong.\n" +
                                    "This cannot be caused by wrong/broken javascript code provided by you since that is within a try/catch block.");
                            System.err.println("PendingJavaScriptResult-ID == "+javaScriptCallback.id);
                            new Exception().printStackTrace();
                            toRemove.add(javaScriptCallback);
                        }
                    }
                    javaScriptCallbacks.removeAll(toRemove);
                }
            } catch (InterruptedException e){
                break;
            } catch (Exception e) {
                System.err.println("Something went wrong in logic of timeoutCheckerThread!");
                e.printStackTrace();
            }
        }
    });
    {
        timeoutCheckerThread.setName("JavaScriptExecutionTimeoutCheckerThread");
    }

    public JSWebSocketServer(String domain, int port) {
        super(new InetSocketAddress(domain, port));
        start();
        this.domain = domain;
        this.port = port;
        timeoutCheckerThread.start();
    }

//    public String jsStartWebSocketClient(String serverDomain, int serverPort) {
//        String url = "ws://" + serverDomain + ":" + serverPort;
//        String jsCode = "try{    var webSocketServer = new WebSocket('" + url + "');\n" +
//                "window.webSocketServer = webSocketServer;\n" + // Make globally accessible
//                "console.log(\"Connecting to WebSocket server...\")\n" +
//                "\n" +
//                "    webSocketServer.addEventListener(\"open\", (event) => {\n" +
//                "      console.log('WebSocket connection established.');\n" +
//                "    });\n" +
//                "\n" +
//                "    webSocketServer.addEventListener(\"message\", (event) => {\n" +
//                "      // Receive a message from the server\n" +
//                "      var receivedMessage = event.data;\n" +
//                (App.isInDepthDebugging ? "      console.log('Received message from server:\\n', receivedMessage);\n" : "") +
//                "      eval(event.data);\n" +
//                "    });\n" +
//                "\n" +
//                "    webSocketServer.addEventListener(\"close\", (event) => {\n" +
//                "      console.log('WebSocket connection closed.');\n" +
//                "    });\n" +
//                "} catch (e) {console.error(e)}\n";
//        return jsCode;
//    }

    public String jsStartWebSocketClient(String serverDomain, int serverPort) {
        String url = "ws://" + serverDomain + ":" + serverPort;
        String jsCode =
                "try {\n" +
                        "    var webSocketServer;\n" +
                        "    var reconnectAttempts = 0;\n" +
                        "    var maxReconnectInterval = 3000; // 3 seconds cap\n" +
                        "\n" +
                        "    function connectWebSocket() {\n" +
                        "        console.log(\"Connecting to WebSocket server...\");\n" +
                        "        webSocketServer = new WebSocket('" + url + "');\n" +
                        "        window.webSocketServer = webSocketServer;\n" + // Make globally accessible
                        "\n" +
                        "        webSocketServer.addEventListener(\"open\", () => {\n" +
                        "            console.log('WebSocket connection established.');\n" +
                        "            reconnectAttempts = 0; // Reset on successful connection\n" +
                        "        });\n" +
                        "\n" +
                        "        webSocketServer.addEventListener(\"message\", (event) => {\n" +
                        "            var receivedMessage = event.data;\n" +
                        (App.isInDepthDebugging ? "            console.log('Received message from server:\\n', receivedMessage);\n" : "") +
                        "            eval(receivedMessage);\n" +
                        "        });\n" +
                        "\n" +
                        "        webSocketServer.addEventListener(\"close\", () => {\n" +
                        "            console.log('WebSocket connection closed. Attempting to reconnect...');\n" +
                        "            scheduleReconnect();\n" +
                        "        });\n" +
                        "\n" +
                        "        webSocketServer.addEventListener(\"error\", (error) => {\n" +
                        "            console.error('WebSocket error:', error);\n" +
                        "            webSocketServer.close(); // Force close to trigger reconnect\n" +
                        "        });\n" +
                        "    }\n" +
                        "\n" +
                        "    function scheduleReconnect() {\n" +
                        "        let reconnectDelay = Math.min(1000 * Math.pow(2, reconnectAttempts), maxReconnectInterval);\n" +
                        "        console.log(`Reconnecting in ${reconnectDelay / 1000} seconds...`);\n" +
                        "        setTimeout(connectWebSocket, reconnectDelay);\n" +
                        "        reconnectAttempts++;\n" +
                        "    }\n" +
                        "\n" +
                        "    connectWebSocket(); // Initial connection\n" +
                        "} catch (e) { console.error(e); }\n";

        return jsCode;
    }

    /**
     * Executes JavaScript (JS) code now and returns directly.
     * The {@link Event} gets triggered once JS code execution finishes/fails.<br>
     * <br>
     * Do not worry if you add an action/listener to the event after it was triggered, because this is a {@link Event}
     * and your listener will still be run. <br>
     * <br>
     * Since we use JavaScript WebSockets, its ensured that the sent JS code is executed in an orderly, synchronous fashion.
     * Meaning that the JS code in the second call of this method, gets executed after the code in the first call finished/failed.<br>
     * <br>
     *
     */
    public void executeJavaScript(UI ui, Event<JavaScriptResult> event, String code){
        Registration registration = addPermanentCallback(ui, code, result -> {
            event.execute(result);
        });

        registration.pendingJavaScriptResult.isPermanent = false;
        code = registration.jsCode; // Registration adds some extra js code
        code = "// PendingJavaScriptResult-ID == "+registration.pendingJavaScriptResult.id+"\n"+ code;
        broadcast(code); // Execute code in client
    }

    public static class Registration{
        public String jsCode;
        public PendingJavaScriptResult pendingJavaScriptResult;

        public Registration(String jsCode, PendingJavaScriptResult pendingJavaScriptResult) {
            this.jsCode = jsCode;
            this.pendingJavaScriptResult = pendingJavaScriptResult;
        }
    }

    /**
     * Returns new JS (JavaScript) code, that when executed in client browser
     * results in onJSFunctionExecuted being executed. <br><br>
     * <p>
     * It wraps around your jsCode and adds callback related stuff, as well as error handling. <br><br>
     * <p>
     * Its a permanent callback, because the returned JS code can be executed multiple times
     * which results in onJSFunctionExecuted or onJSFuntionFailed to get executed multiple times too. <br><br>
     * <p>
     * Also appends a check to the JS code that sets message="" if it was null/undefined.<br><br>
     *
     * @param jsCode    modify the message variable in the provided JS (JavaScript) code to send information from JS to Java.
     *                  Your JS code could look like this: <br>
     *                  message = 'first second third etc...';
     * @param onFinished executed when the provided jsCode executes successfully. String contains the message variable that can be set in your jsCode.
     *                   Or executed when the provided jsCode threw an exception/failed. String contains details about the exception/error in that case.
     */
    public Registration addPermanentCallback(UI ui, String jsCode, Consumer<JavaScriptResult> onFinished) {
        // 1. execute client JS
        // 2. execute callback in java with params from client JS
        // 3. return success to client JS and execute it
        int id = counter.getAndIncrement();
        PendingJavaScriptResult pendingJavaScriptResult;
        synchronized (javaScriptCallbacks) {
            pendingJavaScriptResult = new PendingJavaScriptResult(id, onFinished, ui);
            javaScriptCallbacks.add(pendingJavaScriptResult);
        }
        return new Registration("var message = '';\n" + // Separated by space
                "var error = null;\n" +
                "try{" + jsCode + "\n" +
                "if(message==null) message = '';\n" +
                "} catch (e) { error = e; console.error(e);}\n" +
                jsClientSendWebSocketMessage("(error == null ? ('" + id + " '+message) : ('!" + id + " '+error))"), pendingJavaScriptResult);
    }

    public String jsClientSendWebSocketMessage(String message) {
        return "if(webSocketServer.readyState != 1) {\n" +
                //"console.log(`Frontend and Backend are connected!`)\n" + // 1 == OPEN
                "  webSocketServer.addEventListener(\"open\", (event) => {\n" +
                "      webSocketServer.send(" + message + ");\n" +
                "  });\n" +
                "} else webSocketServer.send(" + message + ");\n";
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        timeoutCheckerThread.interrupt();
        AL.info("Closed websocket server probably due to inactivity "+this.domain+":"+this.port+" - "+this.toString());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // message format: id <message>
        int iFirstSpace = message.indexOf(" ");
        boolean isError = message.startsWith("!");
        int id = isError ? Integer.parseInt(message.substring(1, iFirstSpace)) :
                Integer.parseInt(message.substring(0, iFirstSpace));
        int iPendingResultToRemove = -1;

        // To avoid deadlocks do not execute ui.access inside a synchronized javaScriptCallbacks block
        @Nullable PendingJavaScriptResult pendingResult = null;
        synchronized (javaScriptCallbacks) {
            for (int i = 0; i < javaScriptCallbacks.size(); i++) {
                PendingJavaScriptResult pr = javaScriptCallbacks.get(i);
                if (pr.id == id) {
                    pendingResult = pr;
                    if (!pendingResult.isPermanent) iPendingResultToRemove = i;
                    break;
                }
            }
            if (iPendingResultToRemove != -1)
                javaScriptCallbacks.remove(iPendingResultToRemove);
        }

        // Execute the pending javascript result event in the correct UI context
        if(pendingResult != null){
            final var finalPendingResult = pendingResult;
            if (!isError) // message looks like this: "!3 Error details..." 3 is the id and can be any number
                pendingResult.ui.access(() -> finalPendingResult.onFinished.accept(new JavaScriptResult(message.substring(iFirstSpace + 1), true)));
            else // message looks like this: "3 first second etc..." 3 is the id and can be any number, the rest can be any data or even empty
                pendingResult.ui.access(() -> finalPendingResult.onFinished.accept(new JavaScriptResult(message.substring(iFirstSpace + 1), false)));
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
    }
}