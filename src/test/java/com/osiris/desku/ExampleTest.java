package com.osiris.desku;

import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.layout.Vertical;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExampleTest {

    @Test
    void test() throws Throwable {
        TApp.testAndAwaitResult((asyncResult) -> {
            String defValue = "Test";
            Text txt = new Text(defValue);
            // Code that should run directly

            return new Vertical()
                    .add(txt)
                    .later(__ -> {
                        try{
                            while(UI.get().isLoading()) Thread.yield(); // Wait to ensure not the internal value is directly returned
                            // but instead the value is returned from the frontend HTML value attribute of the component.

                            txt.setValue("1");
                            assertEquals("1", txt.getValue());

                        } catch (Throwable e) {
                            asyncResult.refEx.set(e);
                        } finally {
                            asyncResult.isDone.set(true);
                        }
                    });
        });
    }
}
