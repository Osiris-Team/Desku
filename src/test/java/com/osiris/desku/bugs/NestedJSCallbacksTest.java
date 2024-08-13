package com.osiris.desku.bugs;

import com.osiris.desku.TApp;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.input.CheckBox;
import com.osiris.desku.ui.input.TextField;
import com.osiris.desku.ui.layout.Vertical;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NestedJSCallbacksTest {

    @Test
    void test() throws Throwable {
        TApp.testAndAwaitResult((asyncResult) -> {
            TextField c = new TextField("Label 1");
            UI ui = UI.get();

            return new Vertical()
                    .add(c)
                    .later(ly -> {
                        try{
                            while(ui.isLoading()) Thread.yield(); // Wait to ensure not the internal value is directly returned
                            // but instead the value is returned from the frontend HTML value attribute of the component.

                            c.setValue("Hello");

                            AtomicReference ref = new AtomicReference();
                            c.getValue(v -> {
                               c.getValue(v1 -> {
                                   c.getValue(v2 -> {
                                       Objects.requireNonNull(UI.get());
                                       TextField tf = new TextField("");
                                       tf.setValue("World!");
                                       ref.set(v2 +" "+ tf.getValue());
                                   });
                               });
                            });

                            while(ref.get() == null) Thread.sleep(100);
                            assertEquals("Hello World!", ref.get());


                        } catch (Throwable e) {
                            asyncResult.refEx.set(e);
                        } finally {
                            asyncResult.isDone.set(true);
                        }
                    });
        });

    }
}
