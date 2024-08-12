package com.osiris.desku.bugs;

import com.osiris.desku.TApp;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.input.TextField;
import com.osiris.desku.ui.layout.Vertical;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReplacingTest {

    @Test
    void test() throws Throwable {
        TApp.testAndAwaitResult((asyncResult) -> {
            Vertical ly = new Vertical();
            TextField tf1 = new TextField("TF1", "");
            TextField tf2 = new TextField("TF2", "");
            ly.add(tf1);

            ly.replace(tf1, tf2);

            assertEquals(tf2, ly.children.get(0));

            return ly
                    .later(__ -> {
                        try{
                            while(UI.get().isLoading()) Thread.yield(); // Wait to ensure not the internal value is directly returned
                            // but instead the value is returned from the frontend HTML value attribute of the component.

                            ly.replace(tf2, tf1);

                            assertEquals(tf1, ly.children.get(0));

                        } catch (Throwable e) {
                            asyncResult.refEx.set(e);
                        } finally {
                            asyncResult.isDone.set(true);
                        }
                    });
        });

    }
}
