package com.osiris.desku.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GodIterator {
    public static <T> void forEach(T[] array, Consumer<T> c) {
        for (T t : array) {
            c.accept(t);
        }
    }

    public static <T> void forEach(Iterable<T> array, Consumer<T> c) {
        for (T t : array) {
            c.accept(t);
        }
    }

    public static <T> void forEach(Enumeration<T> array, Consumer<T> c) {
        while (array.hasMoreElements()) {
            c.accept(array.nextElement());
        }
    }

    /**
     * Also provides the iterating list as parameter.
     */
    public static <T> void forEach(T[] array, BiConsumer<T, ArrayList<T>> c) {
        ArrayList<T> list = new ArrayList<>();
        Collections.addAll(list, array);
        for (T t : list) {
            c.accept(t, list);
        }
    }

    /**
     * Also provides the iterating list as parameter.
     */
    public static <T> void forEach(Iterable<T> array, BiConsumer<T, ArrayList<T>> c) {
        ArrayList<T> list = new ArrayList<>();
        for (T t : array) {
            list.add(t);
        }
        for (T t : list) {
            c.accept(t, list);
        }
    }

    /**
     * Also provides the iterating list as parameter.
     */
    public static <T> void forEach(Enumeration<T> array, BiConsumer<T, ArrayList<T>> c) {
        ArrayList<T> list = new ArrayList<>();
        while (array.hasMoreElements()) {
            list.add(array.nextElement());
        }
        for (T t : list) {
            c.accept(t, list);
        }
    }

}
