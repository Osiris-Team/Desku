package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.input.Button;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static com.osiris.desku.Statics.*;

public class Popup extends Overlay {
    public Vertical dialog;
    public Vertical content;
    public Vertical header;
    public @Nullable Text title;
    public Vertical body;
    public @Nullable Horizontal footer;
    public @Nullable Button btn1, btn2;
    public Consumer<AddedChildEvent> _addToBackground = _add;
    //public boolean isCloseByBackgroundClick = true;
    public boolean isCloseOnBtnClick = true;

    public Popup() {
        this(null, button("Okay"), button("Close"));
    }

    /**
     *
     * @param title
     * @param btn1 closes the popup on click through data-bs-dismiss="modal"
     * @param btn2 closes the popup on click throguh data-bs-dismiss="modal"
     */
    public Popup(@Nullable Text title, @Nullable Button btn1, @Nullable Button btn2) {
        super(null);
        addClass("fade show");
        add(dialog = vertical()
                .add(content = vertical().childGap(true)
                        .add(header = vertical().childGap(true))
                        .add(body = vertical().childGap(true))
                        .add(footer = horizontal().childGap(true))
                )
        );
        sty("min-width", "100vw");
        sty("min-height", "100vh");
        sty("background-color", "var(--bs-secondary-color)");
        childCenter1().childCenter2();
        dialog.padding(true);
        dialog.sty("background-color", "var(--bs-light)");
        dialog.sty("min-width", "300px");
        //dialog.s("border", "medium solid var(--bs-primary)");
        dialog.sty("border-radius", "var(--bs-border-radius)");
        if(title != null){
            this.title = title;
            title.sizeXL();
            header.add(title);
        }
        if(btn2 != null){
            this.btn2 = btn2;
            btn2.secondary().atr("data-bs-dismiss", "modal").grow(1);
            footer.add(btn2);
            btn2.onClick(e-> {if(isCloseOnBtnClick) visible(false);});
        }
        if(btn1 != null){
            this.btn1 = btn1;
            btn1.primary().atr("data-bs-dismiss", "modal").grow(1);
            footer.add(btn1);
            btn1.onClick(e-> {if(isCloseOnBtnClick) visible(false);});
        }

        // Automatically add to body
        _add = body._add;

        // Close on background click
        onClick(e -> {
            // TODO doesnt work due to clickthrough if(isCloseByBackgroundClick) visible(false);
        });
        // TODO lock scrolling
    }
}
