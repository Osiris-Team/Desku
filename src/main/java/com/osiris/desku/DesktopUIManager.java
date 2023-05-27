package com.osiris.desku;

public class DesktopUIManager extends UIManager {

    @Override
    public UI create(Route route) throws Exception {
        return new DesktopUI(route);
    }

    @Override
    public UI create(Route route, boolean isTransparent, int widthPercent, int heightPercent) throws Exception {
        return new DesktopUI(route, isTransparent, widthPercent, heightPercent);
    }
}
