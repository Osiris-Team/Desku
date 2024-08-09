package com.osiris.desku.bugs;

import com.osiris.desku.App;
import com.osiris.desku.TApp;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.input.CheckBox;
import com.osiris.desku.ui.input.filechooser.FileAsRow;
import com.osiris.desku.ui.input.filechooser.FileChooser;
import com.osiris.desku.ui.layout.Vertical;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class FileChooserTest {

    @Test
    void test() throws Throwable {
        TApp.testAndAwaitResult((asyncResult) -> {
            FileChooser c = new FileChooser("FC1");
            assertEquals("", c.getValue());

            c.setValue(App.workingDir.getAbsolutePath()+"  ;  ");
            assertEquals(App.workingDir.getAbsolutePath()+"  ;  ", c.getValue());

            FileAsRow f = null;
            for (FileAsRow __ : c.directoryView.selectedFiles) {
                if(__.file.getAbsolutePath().equals(App.workingDir.getAbsolutePath()))
                    f = __;
            }

            assertEquals(App.workingDir.getAbsolutePath(), f != null ? f.file.getAbsolutePath() : null);

            return new Vertical()
                    .add(c)
                    .later(v -> {
                        try{
                            if(true) while(true)
                                Thread.sleep(1111);
                            while(UI.get().isLoading()) Thread.yield(); // Wait to ensure not the internal value is directly returned
                            // but instead the value is returned from the frontend HTML value attribute of the component.

                            c.setValue(App.workingDir.getAbsolutePath()+"  ;  ");

                            assertEquals(App.workingDir.getAbsolutePath()+"  ;  ", c.getValue());

                            // Test default values
                            List<File> defaultFiles = new ArrayList<>();
                            for (File f2 : App.workingDir.listFiles()) {
                                if(new Random().nextInt(5) == 1)
                                    defaultFiles.add(f2);
                            }
                            assertTrue(App.workingDir.listFiles().length != 0);
                            if(defaultFiles.isEmpty()) defaultFiles.add(App.workingDir.listFiles()[0]);

                            FileChooser c2 = new FileChooser("FC2", defaultFiles);
                            assertEquals(FileChooser.pathsListToString(defaultFiles), c2.getValue());



                        } catch (Throwable e) {
                            asyncResult.refEx.set(e);
                        } finally {
                            asyncResult.isDone.set(true);
                        }
                    });
        });

    }
}
