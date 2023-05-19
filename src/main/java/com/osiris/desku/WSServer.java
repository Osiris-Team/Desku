package com.osiris.desku;

import com.osiris.jlib.logger.AL;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WSServer {
    public final String domain;
    public final int port;

    public final Server server;
    public final ServerConnector connector;
    /**
     * Not thread safe unless inside synchronized block for this variable.
     */
    public final List<UI.PendingJavaScriptResult> javaScriptCallbacks = new ArrayList<>();
    public final AtomicInteger counter = new AtomicInteger();
    public WSServer(String domain, int port) throws Exception {
        this.server = new Server(new InetSocketAddress(domain, port));
        connector = new ServerConnector(server);
        server.addConnector(connector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Configure specific websocket behavior
        JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) ->
        {
            // Configure default max size
            wsContainer.setMaxTextMessageSize(Long.MAX_VALUE);
            wsContainer.setIdleTimeout(Duration.of(Long.MAX_VALUE, ChronoUnit.MILLIS));

            // Add websockets
            wsContainer.addMapping("/*", (req, resp) -> new WebSocketAdapter() {
                @Override
                public void onWebSocketText(String message) {
                    //AL.debug(this.getClass(), "onWebSocketText: "+message);
                    // message format: id <message>
                    synchronized (javaScriptCallbacks) {
                        int iFirstSpace = message.indexOf(" ");
                        boolean isError = message.startsWith("!");
                        int id = isError ? Integer.parseInt(message.substring(1, iFirstSpace)) :
                                Integer.parseInt(message.substring(0, iFirstSpace));
                        int iPendingResultToRemove = -1;

                        for (int i = 0; i < javaScriptCallbacks.size(); i++) {
                            UI.PendingJavaScriptResult pendingResult = javaScriptCallbacks.get(i);
                            if (pendingResult.id == id) {
                                if (!pendingResult.isPermanent) iPendingResultToRemove = i;
                                if (!isError) // message looks like this: "!3 Error details..." 3 is the id and can be any number
                                    pendingResult.onSuccess.accept(message.substring(iFirstSpace + 1));
                                else // message looks like this: "3 first second etc..." 3 is the id and can be any number
                                    pendingResult.onError.accept(message.substring(iFirstSpace + 1));
                                break;
                            }
                        }
                        if (iPendingResultToRemove != -1)
                            javaScriptCallbacks.remove(iPendingResultToRemove);
                    }
                }
            });
        });
        this.server.start();
        this.port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        this.domain = domain;
    }
}
