package com.osiris.desku.bugs;

import com.osiris.desku.TApp;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.input.CheckBox;
import com.osiris.desku.ui.input.OptionField;
import com.osiris.desku.ui.layout.Vertical;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckBoxTest {

    @Test
    void test() throws Throwable {
        TApp.testAndAwaitResult((asyncResult) -> {
            CheckBox c = new CheckBox("CB1");
            assertEquals(false, c.getValue());

            c.setValue(true);
            assertEquals(true, c.getValue());

            return new Vertical()
                    .add(c)
                    .later(v -> {
                        try{
                            while(UI.get().isLoading()) Thread.yield(); // Wait to ensure not the internal value is directly returned
                            // but instead the value is returned from the frontend HTML value attribute of the component.

                            c.setValue(false);
                            assertEquals(false, c.getValue());

                            c.setValue(true);
                            assertEquals(true, c.getValue());

                            var cb2 = new CheckBox("CB2");
                            v.add(cb2);
                            assertEquals(false, cb2.getValue());

                            cb2.setValue(true);
                            assertEquals(true, cb2.getValue());


                        } catch (Throwable e) {
                            asyncResult.refEx.set(e);
                        } finally {
                            asyncResult.isDone.set(true);
                        }
                    });
        });

    }
}
