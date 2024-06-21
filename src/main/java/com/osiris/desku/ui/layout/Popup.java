package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.input.Button;
import com.osiris.desku.ui.utils.NoValue;
import org.jetbrains.annotations.Nullable;

import static com.osiris.desku.Statics.vertical;

public class Popup extends Component<Popup, NoValue> {
    public Vertical dialog;
    public Vertical content;
    public Vertical header;
    public @Nullable Text title;
    public Vertical body;
    public @Nullable Vertical footer;
    public @Nullable Button btn1, btn2;

    public Popup() {
        this(null, null, null);
    }

    /**
     *
     * @param title
     * @param btn1 closes the popup on click through data-bs-dismiss="modal"
     * @param btn2 closes the popup on click throguh data-bs-dismiss="modal"
     */
    public Popup(@Nullable Text title, @Nullable Button btn1, @Nullable Button btn2) {
        super(NoValue.GET, "popup");
        a("class", "modal fade show");
        add(dialog = vertical().a("class", "modal-dialog modal-dialog-centered modal-dialog-scrollable")
                        .add(content = vertical().a("class", "modal-content")
                                .add(header = vertical().a("class", "modal-content"))
                                .add(body = vertical().a("class", "modal-body"))
                                .add(footer = vertical().a("class", "modal-footer")))
        );
        if(title != null){
            this.title = title;
            title.sizeXL();
            header.add(title);
        }
        if(btn2 != null){
            this.btn2 = btn2;
            btn2.secondary().a("data-bs-dismiss", "modal");
            footer.add(btn2);
        }
        if(btn1 != null){
            this.btn1 = btn1;
            btn1.primary().a("data-bs-dismiss", "modal");
            footer.add(btn1);
        }
    }
}
