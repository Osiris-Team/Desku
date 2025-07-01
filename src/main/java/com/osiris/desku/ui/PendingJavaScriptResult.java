package com.osiris.desku.ui;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PendingJavaScriptResult {
    public final int id;
    public final long msCreated = System.currentTimeMillis();
    public final Consumer<JavaScriptResult> onFinished;
    public boolean isPermanent = true;
    public @NotNull UI ui;

    public PendingJavaScriptResult(int id, Consumer<JavaScriptResult> onFinished, @NotNull UI ui) {
        this.id = id;
        this.onFinished = onFinished;
        this.ui = ui;
    }
}
