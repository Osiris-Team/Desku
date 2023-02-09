package com.osiris.desku.swing.events;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandler;

public class LoadStateChange {
    public final CefBrowser browser;
    /**
     * Only not null on error.
     */
    public final CefFrame frame;
    /**
     * Only not null on error.
     */
    public final CefLoadHandler.ErrorCode errorCode;
    /**
     * Only not null on error.
     */
    public final String errorText;
    /**
     * Only not null on error.
     */
    public final String failedUrl;
    public final boolean isLoading, canGoBack, canGoForward;

    public LoadStateChange(CefBrowser browser, CefFrame frame, CefLoadHandler.ErrorCode errorCode,
                           String errorText, String failedUrl, boolean isLoading, boolean canGoBack, boolean canGoForward) {
        this.browser = browser;
        this.frame = frame;
        this.errorCode = errorCode;
        this.errorText = errorText;
        this.failedUrl = failedUrl;
        this.isLoading = isLoading;
        this.canGoBack = canGoBack;
        this.canGoForward = canGoForward;
    }

    public boolean isError(){
        return errorCode != null || errorText != null || failedUrl != null;
    }
}
