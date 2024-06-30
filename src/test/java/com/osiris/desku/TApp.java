package com.osiris.desku;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.DesktopUIManager;
import com.osiris.desku.ui.UI;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Test App used globally for all tests.
 */
public class TApp {
    public static MRoute route;
    public static UI ui;

    public static class AsyncResult{
        public AtomicBoolean isDone = new AtomicBoolean(false);
        public AtomicReference<Throwable> refEx = new AtomicReference<>();
    }

    public static AsyncResult testAndAwaitResult(Function<AsyncResult, Component<?,?>> onLoad){
        AsyncResult asyncResult = new AsyncResult();
        App.init(new DesktopUIManager());
        route = new MRoute("/", () -> onLoad.apply(asyncResult));
        try {
            ui = App.uis.create(route);
            while(ui.isLoading()) Thread.yield();

            for (int i = 0; i < 30; i++) {
                if(asyncResult.isDone.get()) break;
                Thread.sleep(1000);
            }
            if(!asyncResult.isDone.get()) throw new Exception("Didn't finish within 30 seconds!");
            if(asyncResult.refEx.get() != null) throw asyncResult.refEx.get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return asyncResult;
    }
}
