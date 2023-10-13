package com.osiris.desku;

import com.osiris.desku.ui.Component;

import java.util.function.Supplier;

/**
 * MRoute (MutableRoute) uses a public, modifiable function that gets executed at {@link Route#loadContent()},
 * which makes it possible to use lambdas.
 */
public class MRoute extends Route {
    public Supplier<Component<?,?>> onLoad;

    public MRoute(String path, Supplier<Component<?,?>> onLoad) {
        super(path);
        this.onLoad = onLoad;
    }

    @Override
    public Component<?,?> loadContent() {
        return onLoad.get();
    }
}
