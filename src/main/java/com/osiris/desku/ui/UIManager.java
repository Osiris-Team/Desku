package com.osiris.desku.ui;

import com.osiris.desku.MRoute;
import com.osiris.desku.Route;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public abstract class UIManager {
    public final CopyOnWriteArrayList<UI> all = new CopyOnWriteArrayList<>();

    private boolean containsUIWithRouteName(String routeName){
        for (UI ui : all) {
            if (ui.route.path.equals(routeName)) return true;
        }
        return false;
    }

    /**
     * Creates a new random mutable {@link Route} and a new {@link UI},
     * then loads the provided content and returns the {@link UI}.
     */
    public UI create(Supplier<Component<?,?>> onLoad) throws Exception {
            String randomRouteName;
            while(containsUIWithRouteName(
                    randomRouteName = "/"+new Random().nextInt()));
            Route route = new MRoute(randomRouteName, onLoad);
            return create(route);

    }

    public abstract UI create(Route route) throws Exception;

    public abstract UI create(Route route, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception;
}
