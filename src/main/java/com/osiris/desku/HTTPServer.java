package com.osiris.desku;


import com.osiris.jlib.logger.AL;
import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class HTTPServer {
    public final int serverPort;
    public final String serverDomain;
    public UI ui;
    public NanoHTTPD server;

    public HTTPServer(UI ui, String serverDomain, int serverPort) throws Exception {
        this.ui = ui;
        this.server = new NanoHTTPD(serverDomain, serverPort) {
            @Override
            public Response serve(IHTTPSession session) {
                String path = session.getUri();
                //super.doHandle(target, baseRequest, request, response);
                String fileTarget = path;
                if (!path.contains(".")) fileTarget += ".html";
                AL.debug(this.getClass(), "Request: \"" + path + "\" aka file: \"" + fileTarget + "\" " + session.getMethod());
                File f = new File(App.htmlDir + fileTarget);
                if (!f.exists()) {
                    String err = "File/Content for " + path + " does not exist, checked dirs: " + App.htmlDir;
                    String msg = "<html><body><h1>Error!</h1>\n";
                    msg += "<p>" + err + "</p>";
                    AL.warn(err);
                    return sendHTMLString(msg + "</body></html>\n");
                }
                try {
                    AL.info("File: " + f);
                    return sendFile(f);
                } catch (Exception e) {
                    String err = "Failed to provide content for " + path + " due to an exception '" + e.getMessage() + "' (more details in the log). File: " + f;
                    AL.warn(err, e);
                    String msg = "<html><body><h1>Error!</h1>\n";
                    msg += "<p>" + err + "</p>";
                    return sendHTMLString(msg + "</body></html>\n");
                }
            }
        };
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        this.serverPort = serverPort;
        this.serverDomain = serverDomain;
    }

    private static NanoHTTPD.Response sendHTMLString(String s) {
        NanoHTTPD.Response r = r = newFixedLengthResponse(s);
        r.setMimeType("text/html");
        return r;
    }

    private static NanoHTTPD.Response sendFile(File file) {
        NanoHTTPD.Response r = null;
        try {
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType.contains("text")) {
                r = newFixedLengthResponse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
                r.setMimeType(mimeType);
            } else {
                byte[] bytes = Files.readAllBytes(file.toPath());
                ByteArrayInputStream input = new ByteArrayInputStream(bytes);
                r = newFixedLengthResponse(new NanoHTTPD.Response.IStatus() {
                    @Override
                    public String getDescription() {
                        return null;
                    }

                    @Override
                    public int getRequestStatus() {
                        return 200;
                    }
                }, mimeType, input, bytes.length);
                r.setMimeType(mimeType);
            }
            AL.debug(HTTPServer.class, mimeType + " -> " + file);
        } catch (IOException e) {
            AL.warn("Failed to set MIME type of response!", e);
        }
        return r;
    }

}
