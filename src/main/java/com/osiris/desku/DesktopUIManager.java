package com.osiris.desku;

import com.osiris.desku.swing.LoadingWindow;
import com.osiris.desku.swing.events.LoadStateChange;
import com.osiris.jlib.logger.AL;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefLoadHandlerAdapter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class DesktopUIManager extends UIManager {
    public final boolean isOffscreenRendering;
    public final CefApp cef;
    public final CefClient cefClient;
    public final CefMessageRouter cefMessageRouter;
    public final AtomicInteger cefMessageRouterRequestId = new AtomicInteger();

    public DesktopUIManager(boolean isOffscreenRendering) throws UnsupportedPlatformException, CefInitializationException, IOException, InterruptedException {
        this.isOffscreenRendering = isOffscreenRendering;
        // (0) Initialize CEF using the maven loader
        CefAppBuilder builder = new CefAppBuilder();
        try {
            builder.setProgressHandler(new LoadingWindow().getProgressHandler());
        } catch (Exception e) {
            // Expected to fail on Android/iOS
            throw new RuntimeException("Failed to open startup loading window, thus not displaying/logging JCEF load status.", e);
        }
        builder.getCefSettings().windowless_rendering_enabled = isOffscreenRendering;
        // USE builder.setAppHandler INSTEAD OF CefApp.addAppHandler!
        // Fixes compatibility issues with MacOSX
        builder.setAppHandler(new MavenCefAppHandlerAdapter() {
            @Override
            public void stateHasChanged(org.cef.CefApp.CefAppState state) {
                // Shutdown the app if the native CEF part is terminated
                if (state == CefApp.CefAppState.TERMINATED) System.exit(0);
            }
        });
        // (1) The entry point to JCEF is always the class CefApp.
        cef = builder.build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CefApp.getInstance().dispose();
        }));
        // (2) JCEF can handle one to many browser instances simultaneous.
        cefClient = cef.createClient();
        // (3) Create a simple message router to receive messages from CEF.
        CefMessageRouter.CefMessageRouterConfig config = new CefMessageRouter.CefMessageRouterConfig();
        config.jsQueryFunction = "cefQuery";
        config.jsCancelFunction = "cefQueryCancel";
        cefMessageRouter = CefMessageRouter.create(config);
        cefClient.addMessageRouter(cefMessageRouter);
        // Handle load
        cefClient.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode, String errorText, String failedUrl) {
                AL.info("onLoadError: " + browser.getURL() + " errorCode: " + errorCode + " errorText: " + errorText);
                for (UI _uis : uis) {
                    DesktopUI ui = (DesktopUI) _uis;
                    if (ui.browser != null && ui.browser == browser)
                        ui.onLoadStateChanged.execute(new LoadStateChange(browser, frame, errorCode,
                                errorText, failedUrl, false, false, false));
                }
            }

            @Override
            public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
                AL.info("onLoadingStateChange: " + browser.getURL() + " isLoading: " + isLoading);
                for (UI _uis : uis) {
                    DesktopUI ui = (DesktopUI) _uis;
                    if (ui.browser != null && ui.browser == browser)
                        ui.onLoadStateChanged.execute(new LoadStateChange(browser, null, null,
                                null, null, isLoading, canGoBack, canGoForward));
                }
            }
        });
    }

    @Override
    UI createUI(Route route) throws IOException {
        return new DesktopUI(route);
    }

    @Override
    UI createUI(Route route, boolean isTransparent, int widthPercent, int heightPercent) throws Exception {
        return new DesktopUI(route, isTransparent, widthPercent, heightPercent);
    }
}
