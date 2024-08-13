package com.osiris.desku.ui;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WSServer extends WebSocketServer {
    public final String domain;
    public final int port;
    /**
     * Not thread safe unless inside synchronized block for this variable.
     */
    public final List<UI.PendingJavaScriptResult> javaScriptCallbacks = new ArrayList<>();
    public final AtomicInteger counter = new AtomicInteger();

    public WSServer(String domain, int port) {
        super(new InetSocketAddress(domain, port));
        start();
        this.domain = domain;
        this.port = port;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
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
                        pendingResult.ui.access(() -> pendingResult.onSuccess.accept(message.substring(iFirstSpace + 1)));
                    else // message looks like this: "3 first second etc..." 3 is the id and can be any number
                        pendingResult.ui.access(() ->  pendingResult.onError.accept(message.substring(iFirstSpace + 1)));
                    break;
                }
            }
            if (iPendingResultToRemove != -1)
                javaScriptCallbacks.remove(iPendingResultToRemove);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
    }
}