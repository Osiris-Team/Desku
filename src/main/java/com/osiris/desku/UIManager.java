package com.osiris.desku;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class UIManager {
    public static CopyOnWriteArrayList<UI> uis = new CopyOnWriteArrayList<>();

    abstract UI createUI(Route route) throws Exception;

    abstract UI createUI(Route route, boolean isTransparent, int widthPercent, int heightPercent) throws Exception;
}
