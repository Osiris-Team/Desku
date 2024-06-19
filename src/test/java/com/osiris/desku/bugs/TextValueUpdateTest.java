package com.osiris.desku.bugs;

import com.osiris.desku.TApp;
import com.osiris.desku.ui.display.Text;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TextValueUpdateTest {
    @Test
    void test() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> f = new CompletableFuture<>();
        new Thread(() -> {
            TApp.load(() -> {
                return new Text("Hello!")
                        .later(_t -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            _t.setValue("1");
                        });
            });
        }).start();

        f.get(); // TODO
    }
}
