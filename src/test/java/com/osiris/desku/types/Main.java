package com.osiris.desku.types;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test some type acrobatics.
 */
public class Main {

    public static class SimpleRoot{
        public final CopyOnWriteArrayList<SimpleRoot> children = new CopyOnWriteArrayList<>();
    }

    public static class Root<THIS extends Root<THIS, VALUE>, VALUE>{
        public final CopyOnWriteArrayList<Root> children = new CopyOnWriteArrayList<>();

        public THIS returnsExtendingClass(){
            return (THIS) this;
        }
    }


    public static class A extends Root<A, String> {

    }

    public static class AA extends A{

    }

    public static class B extends Root<B, String> {

    }

    @Test
    void test() {

        A a = new A();
        for (Root child : a.children) {
            // Valid
        }
        for (Root<?, ?> child : a.children) {
            // Valid
        }

        AA aa = new AA();
        for (Root<?, ?> child : aa.children) {
            // Valid
        }

        for (Root child : a.children) {
            for (Object o : child.children) { // <--- Wrong type returned! Why is this Object?

            }
        }
        for (SimpleRoot child : new SimpleRoot().children) {
            for (SimpleRoot simpleRoot : child.children) { // However when no parameters are in play, this works!?

            }
        }

        /**
         * Can we fill lists with any type that extends Root?
         * Yes we can.
         */
        a.children.add(a);
        a.children.add(aa);

        aa.children.add(a);
        aa.children.add(aa);

        B b = new B();
        a.children.add(b);
        aa.children.add(b);
        b.children.add(b);
        b.children.add(a);
        b.children.add(aa);

        /**
         * Allow method chaining of method defined in super class. <br>
         */
        Root shouldBeRoot = new Root().returnsExtendingClass();
        A shouldBeA = new A().returnsExtendingClass();
        B shouldBeB = new B().returnsExtendingClass();
        A shouldBeAA = new AA().returnsExtendingClass(); // <--- Why does this **not** return AA?!
    }

}
