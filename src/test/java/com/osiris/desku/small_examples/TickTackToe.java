package com.osiris.desku.small_examples;

import com.osiris.desku.App;
import com.osiris.desku.ui.DesktopUIManager;
import com.osiris.desku.ui.input.Button;
import com.osiris.desku.ui.layout.Horizontal;
import com.osiris.desku.ui.layout.Vertical;

import java.util.concurrent.atomic.AtomicReference;

import static com.osiris.desku.Statics.*;

public class TickTackToe {
    public static void main(String[] args) throws Exception {
        App.init(new DesktopUIManager());
        App.name = "TickTackToe";
        App.uis.create(() -> {
            Vertical ly = vertical().sizeFull().childSpaceEvenly1();
            AtomicReference<String> player = new AtomicReference<>("x");
            Button[][] buttons = new Button[3][3];
            for (int y = 0; y < 3; y++) { // 3 rows
                Horizontal row = ly.horizontalCL().childSpaceEvenly1();
                for (int x = 0; x < 3; x++) { // 3 buttons per row
                    int finalY = y; int finalX = x;
                    Button button = button("").onClick(e -> {
                        if(!e.comp.getValue().isEmpty()){
                            ly.add(text("This field is not empty! Try again."));
                            return;
                        }
                        e.comp.setValue(player.get()); // Show updated value in UI
                        player.set(player.get().equals("x") ? "o" : "x"); // Change player
                        String winner = hasWinner(buttons, e.comp, finalX, finalY);
                        if(!winner.isEmpty()) {
                            String s = winner+" won!";
                            ly.add(text(s));
                            System.out.println(s);
                        }
                    });
                    buttons[y][x] = button;
                    row.add(button);
                }
            }
            return ly;
        });
    }

    private static String hasWinner(Button[][] buttons, Button btn, int btnX, int btnY) {

        String val = btn.getValue();
        System.out.println(val);
        boolean isHorizontalWin = true;
        for (int x = 0; x < 3; x++) { // Check horizontal neighbors of button
            if(!buttons[btnY][x].getValue().equals(val)) isHorizontalWin = false;
        }
        boolean isVerticalWin = true;
        for (int y = 0; y < 3; y++) { // Check vertical neighbors of button
            if(!buttons[y][btnX].getValue().equals(val)) isVerticalWin = false;
        }
        boolean isDiagonalWin = true;
        for (int i = 0; i < 3; i++) { // Check diagonal top left to bottom right
            if(!buttons[i][i].getValue().equals(val)) isDiagonalWin = false;
        }
        for (int i = 2; i >= 0; i--) { // Check top right to bottom left
            if(!buttons[i][i].getValue().equals(val)) isDiagonalWin = false;
        }
        return isHorizontalWin | isVerticalWin | isDiagonalWin ? val : "";
    }
}
