package com.osiris.desku;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.DesktopUIManager;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.UIManager;
import com.osiris.jlib.logger.AL;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Test App used globally for all tests.
 */
public class TApp {
    public static UI ui;

    public static class AsyncResult{
        public AtomicBoolean isDone = new AtomicBoolean(false);
        public AtomicReference<Throwable> refEx = new AtomicReference<>();
    }

    public static CopyOnWriteArrayList<UIManager> uiManagers = new CopyOnWriteArrayList<>();

    public static AsyncResult testAndAwaitResult(Function<AsyncResult, Component<?,?>> onLoad){
        AsyncResult asyncResult = new AsyncResult();
        App.isInDepthDebugging = true;
        var logger = new App.LoggerParams();
        logger.debug = true;
        var uiManager = new DesktopUIManager();
        uiManagers.add(uiManager);
        App.init(uiManager, logger);
        try {
            ui = uiManager.create(() -> onLoad.apply(asyncResult));
            while(ui.isLoading()) Thread.yield();

            for (int i = 0; i < 30; i++) {
                if(asyncResult.isDone.get()) break;
                Thread.sleep(1000);
            }
            if(!asyncResult.isDone.get()) throw new Exception("Didn't finish within 30 seconds!");
            if(asyncResult.refEx.get() != null) throw asyncResult.refEx.get();
        } catch (Throwable e) {
            AL.info("UIManagers: "+uiManagers.size());
            for (UIManager manager : uiManagers) {
                AL.info("Windows for manager: "+ manager);
                for (UI ui1 : manager.all) {
                    AL.info("ui: "+ ui1+" route: "+ui1.route+" at "+ui1.getSnapshotTempFile());
                }
            }
            throw new RuntimeException(e);
        }
        return asyncResult;
    }

    public static void testIndefinetely(Function<AsyncResult, Component<?,?>> onLoad){
        AsyncResult asyncResult = new AsyncResult();
        App.isInDepthDebugging = true;
        var logger = new App.LoggerParams();
        logger.debug = true;
        App.init(new DesktopUIManager(), logger);
        try {
            ui = App.uis.create(() -> onLoad.apply(asyncResult));
            while(ui.isLoading()) Thread.yield();

            while(true)
                Thread.sleep(1000);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
