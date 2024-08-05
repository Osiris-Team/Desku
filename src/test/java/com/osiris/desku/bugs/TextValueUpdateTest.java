package com.osiris.desku.bugs;

import com.osiris.desku.TApp;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.layout.Vertical;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextValueUpdateTest {

    static class Person{
        public String name = "John";
        public int age = 34;
    }
    static class MyComp extends Component<MyComp, Person>{

        public MyComp() {
            super(new Person(), Person.class);
        }
    }

    @Test
    void test() throws Throwable {
        TApp.testAndAwaitResult((asyncResult) -> {
            String txtDefValue  = "Hello!";
            Text txt = new Text(txtDefValue);
            txt.setValue("0");
            assertEquals("0", txt.getValue());

            MyComp myComp = new MyComp();
            return new Vertical()
                    .add(txt)
                    .add(myComp)
                    .later(__ -> {
                        try{
                            while(UI.get().isLoading()) Thread.yield(); // Wait to ensure not the internal value is directly returned
                            // but instead the value is returned from the frontend HTML value attribute of the component.

                            txt.setValue("1");
                            assertEquals("1", txt.getValue());

                            txt.setValue(null);
                            assertEquals(txtDefValue, txt.getValue());

                            txt.setValue("\"\"");
                            assertEquals("\"\"", txt.getValue());

                            txt.setValue("C:\\example\\path");
                            assertEquals("C:\\example\\path", txt.getValue());

                            txt.setValue("{}"); // NoValue.GET as string is also {}, however the components type is String and not NoValue
                            assertEquals("{}", txt.getValue());

                            txt.setValue("{\"key\": \"value\"}");
                            assertEquals("{\"key\": \"value\"}", txt.getValue());

                            assertTrue(myComp.isDefaultValue());

                            Person p1 = new Person();
                            p1.name = "Maria";
                            p1.age = 22;
                            myComp.setValue(p1);
                            assertTrue(myComp.isValuesEqual(p1, myComp.getValue()));


                        } catch (Throwable e) {
                            asyncResult.refEx.set(e);
                        } finally {
                            asyncResult.isDone.set(true);
                        }
                    });
        });

    }
}
