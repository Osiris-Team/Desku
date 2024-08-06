package com.osiris.desku.bugs;

import com.osiris.desku.TApp;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.input.TextField;
import com.osiris.desku.ui.layout.Vertical;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextFieldValueUpdateTest {

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
            TextField tf = new TextField("Label", txtDefValue);
            MyComp myComp = new MyComp();
            return new Vertical()
                    .add(tf)
                    .add(myComp)
                    .later(__ -> {
                        try{
                            while(UI.get().isLoading()) Thread.yield(); // Wait to ensure not the internal value is directly returned
                            // but instead the value is returned from the frontend HTML value attribute of the component.

                            tf.setValue("1");
                            assertEquals("1", tf.getValue());

                            tf.setValue(null);
                            assertEquals(txtDefValue, tf.getValue());

                            tf.setValue("\"\"");
                            assertEquals("\"\"", tf.getValue());

                            tf.setValue("C:\\example\\path");
                            assertEquals("C:\\example\\path", tf.getValue());

                            tf.setValue("{}"); // NoValue.GET as string is also {}, however the components type is String and not NoValue
                            assertEquals("{}", tf.getValue());

                            tf.setValue("{\"key\": \"value\"}");
                            assertEquals("{\"key\": \"value\"}", tf.getValue());

                            tf.setValue("`");
                            assertEquals("`", tf.getValue());


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
