package com.osiris.desku.ui;

import com.osiris.desku.Route;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class UIManager {
    public static CopyOnWriteArrayList<UI> all = new CopyOnWriteArrayList<>();

    public abstract UI create(Route route) throws Exception;

    public abstract UI create(Route route, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception;
}
