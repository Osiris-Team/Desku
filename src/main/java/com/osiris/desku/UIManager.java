package com.osiris.desku;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class UIManager {
    public static CopyOnWriteArrayList<UI> all = new CopyOnWriteArrayList<>();

    public abstract UI create(Route route) throws Exception;

    public abstract UI create(Route route, boolean isTransparent, int widthPercent, int heightPercent) throws Exception;
}
