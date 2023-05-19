package com.osiris.desku;

import com.osiris.jlib.logger.AL;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class HTTPServer {
    public final int serverPort;
    public final String serverDomain;
    public UI ui;
    public Server server;
    public ServletHandler handler = new ServletHandler() {
        @Override
        public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            //super.doHandle(target, baseRequest, request, response);
            String fileTarget = target;
            if (target.equals("/") || target.isEmpty()) fileTarget = "/root";
            if (!target.contains(".")) fileTarget += ".html";
            AL.info("Request: \"" + target + "\" \"" + fileTarget + "\" " + baseRequest);
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            File f = new File(ui.getDir() + fileTarget);
            if (!f.exists()) {
                String err = "File/Content for " + target + " does not exist: " + f;
                String msg = "<html><body><h1>Error!</h1>\n";
                msg += "<p>" + err + "</p>";
                AL.warn(err);
                writer.println(msg + "</body></html>\n");
                baseRequest.setHandled(true);
                return;
            }
            try {
                AL.info("File: " + f);
                writer.println(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
                baseRequest.setHandled(true);
                response.setStatus(200);
            } catch (Exception e) {
                String err = "Failed to provide content for " + target + " due to an exception '" + e.getMessage() + "' (more details in the log). File: " + f;
                AL.warn(err, e);
                String msg = "<html><body><h1>Error!</h1>\n";
                msg += "<p>" + err + "</p>";
                writer.println(msg + "</body></html>\n");
                baseRequest.setHandled(true);
            }
        }
    };

    public HTTPServer(UI ui, String serverDomain, int serverPort) throws Exception {
        this.ui = ui;
        this.server = new Server(new InetSocketAddress(serverDomain, serverPort));
        // Create a ResourceHandler to handle static files
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{"root.html"});
        resourceHandler.setResourceBase(ui.getDir().getAbsolutePath());

        // Create a DefaultHandler to handle requests that don't match any other handlers
        DefaultHandler defaultHandler = new DefaultHandler();

        // Create a HandlerList to hold multiple handlers
        HandlerList handlerList = new HandlerList();
        handlerList.addHandler(resourceHandler);
        handlerList.addHandler(defaultHandler);

        // Set the HandlerList as the server's handler
        server.setHandler(handlerList);
        server.start();
        this.serverPort = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        this.serverDomain = serverDomain;
    }

}
