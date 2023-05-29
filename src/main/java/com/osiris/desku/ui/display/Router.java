package com.osiris.desku.ui.display;

import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.ui.Component;
import com.osiris.jlib.logger.AL;

public class Router extends Component<Router> {

    public Router() {
        super("a");
    }

    public Router set(String url) {
        putAttribute("href", url);
        return this;
    }

    public Router set(Class<? extends Route> routeClass) {
        Route route = null;
        for (Route r : App.routes) {
            if (r.getClass().equals(routeClass)) {
                route = r;
                break;
            }
        }
        if (route == null) { // Route was not registered
            AL.warn("Failed to set href/link for this router, since provided route '" + routeClass
                    + "' was not registered, aka not added to App.routes!", new Exception());
            return this;
        }
        putAttribute("href", route.path);
        return this;
    }
}
