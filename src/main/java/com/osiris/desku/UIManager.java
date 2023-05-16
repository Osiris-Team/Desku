package com.osiris.desku;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class UIManager {
    public static CopyOnWriteArrayList<UI> uis = new CopyOnWriteArrayList<>();
}
