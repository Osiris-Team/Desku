package com.osiris.desku.bugs;

import com.osiris.desku.TApp;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.input.CheckBox;
import com.osiris.desku.ui.layout.Vertical;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NestedAccessTest {

    @Test
    void test() throws Throwable {
        TApp.testAndAwaitResult((asyncResult) -> {
            CheckBox c = new CheckBox("CB1");
            UI ui = UI.get();
            ui.access(() -> {
                ui.access(() -> {
                    assertEquals(false, c.getValue());
                });
            });

            return new Vertical()
                    .add(c)
                    .later(v -> {
                        try{
                            while(ui.isLoading()) Thread.yield(); // Wait to ensure not the internal value is directly returned
                            // but instead the value is returned from the frontend HTML value attribute of the component.

                            c.setValue(true);

                            ui.access(() -> {
                                ui.access(() -> {
                                    assertEquals(true, c.getValue());
                                });
                            });


                        } catch (Throwable e) {
                            asyncResult.refEx.set(e);
                        } finally {
                            asyncResult.isDone.set(true);
                        }
                    });
        });

    }
}
