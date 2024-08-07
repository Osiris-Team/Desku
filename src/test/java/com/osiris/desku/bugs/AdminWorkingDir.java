package com.osiris.desku.bugs;

import com.osiris.desku.App;
import com.osiris.desku.TApp;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.layout.Vertical;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdminWorkingDir {

    @Test
    void test() throws Throwable {
        System.setProperty("user.dir", "C:\\Program Files");
        TApp.testAndAwaitResult((asyncResult) -> {
            String txtDefValue  = "Hello!";
            Text txt = new Text(txtDefValue);

            assertEquals(App.workingDir, App.userDir);

            return new Vertical()
                    .add(txt)
                    .later(__ -> {
                        try{
                            while(UI.get().isLoading()) Thread.yield(); // Wait to ensure not the internal value is directly returned
                            // but instead the value is returned from the frontend HTML value attribute of the component.
                        } catch (Throwable e) {
                            asyncResult.refEx.set(e);
                        } finally {
                            asyncResult.isDone.set(true);
                        }
                    });
        });

    }
}
