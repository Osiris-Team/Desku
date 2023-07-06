package com.osiris.desku.ui;

import com.osiris.desku.Route;

public class DesktopUIManager extends UIManager {

    @Override
    public UI create(Route route) throws Exception {
        return new DesktopUI(route);
    }

    @Override
    public UI create(Route route, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception {
        return new DesktopUI(route, isTransparent, isDecorated, widthPercent, heightPercent);
    }
}
