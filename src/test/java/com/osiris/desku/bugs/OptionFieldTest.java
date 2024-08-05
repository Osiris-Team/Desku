package com.osiris.desku.bugs;

import com.osiris.desku.TApp;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.input.OptionField;
import com.osiris.desku.ui.layout.Vertical;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptionFieldTest {

    @Test
    void test() throws Throwable {
        TApp.testAndAwaitResult((asyncResult) -> {
            String[] defItems = new String[]{"option1", "option2", "option3"};
            OptionField optionField = new OptionField();
            optionField.addItems(defItems);

            optionField.setValue("option0");
            assertEquals("option0", optionField.getValue());

            return new Vertical()
                    .add(optionField)
                    .later(__ -> {
                        try{
                            while(UI.get().isLoading()) Thread.yield(); // Wait to ensure not the internal value is directly returned
                            // but instead the value is returned from the frontend HTML value attribute of the component.

                            optionField.addItems(defItems);
                            assertEquals(defItems.length * 2, optionField.items.get().size());

                            optionField.setValue("option1");
                            assertEquals("option1", optionField.getValue());

                        } catch (Throwable e) {
                            asyncResult.refEx.set(e);
                        } finally {
                            asyncResult.isDone.set(true);
                        }
                    });
        });

    }
}
