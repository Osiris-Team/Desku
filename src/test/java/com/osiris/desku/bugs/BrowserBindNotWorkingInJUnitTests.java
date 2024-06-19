package com.osiris.desku.bugs;

import dev.webview.Webview;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * It works!
 */
public class BrowserBindNotWorkingInJUnitTests {
    @Test
    public void test() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        // The below is taken from SwingExample.java
        JFrame frame = new JFrame();

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Using createAWT allows you to defer the creation of the webview until the
        // canvas is fully renderable.
        Component component = Webview.createAWT(true, (wv) -> {
            // Calling `await echo(1,2,3)` will return `[1,2,3]`
            wv.bind("echo", (arguments) -> {
                future.complete(null);
                return arguments;
            });

            //wv.loadURL("file:///"+new File(System.getProperty("user.dir")+"/test.html").getAbsolutePath());
            wv.loadURL("about:blank");
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    wv.close();
                    frame.dispose();
                    System.exit(0);
                }
            });

            new Thread(() -> {
                wv.dispatch(() -> {
                    wv.eval(
                            "console.log('Executed JavaScript window.echo(...)! Test should finish now!')\n" +
                                    "window.echo('hi')");
                    System.out.println("Executed JavaScript window.echo(...)! Test should finish now!");
                });
            }).start();

            // Run the webview event loop, the webview is fully disposed when this returns.
            wv.run();
        });

        frame.getContentPane().add(component, BorderLayout.CENTER);


        frame.setTitle("My Webview App");
        frame.setSize(800, 600);
        frame.setVisible(true);
        future.get();
    }
}
