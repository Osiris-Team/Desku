package com.osiris.desku.ui.display;

import com.osiris.desku.App;
import com.osiris.desku.ui.Component;
import com.osiris.events.Event;

import java.io.IOException;
import java.util.function.Consumer;

public class Table extends Component<Table> {
    static {
        try {
            App.appendToGlobalCSS(App.getCSS(Table.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Headers headers = new Headers(this);
    public Rows rows = new Rows(this);

    public Table() {
        add(headers, rows);
        addClass("desku-table");
        //putStyle("display", "block"); // instead of table since that gives additional whitespace
    }

    /**
     * Easily set/replace the headers.
     */
    public Table headers(String... headers) {
        this.headers.removeAll();
        for (String header : headers) {
            this.headers.add(new Header().add(new Text(header)));
        }
        return _this;
    }

    /**
     * Easily set/replace the headers.
     */
    public Table headers(Header... headers) {
        this.headers.removeAll();
        for (Header header : headers) {
            this.headers.add(header);
        }
        return _this;
    }

    /**
     * Easily append a complete new row.
     */
    public Table row(String... data) {
        Row row = new Row();
        for (String s : data) {
            row.add(new Text(s));
        }
        rows.add(row);
        return this;
    }

    /**
     * Easily append a complete new row.
     */
    public Table row(Row row) {
        rows.add(row);
        return this;
    }

    /**
     * Easily set/replace the complete row at an index.
     *
     * @throws IndexOutOfBoundsException
     */
    public Table row(int index, String... data) {
        Row oldRow = (Row) rows.children.get(index);
        Row newRow = new Row();
        for (String s : data) {
            newRow.add(new Text(s));
        }
        rows.replace(oldRow, newRow);
        return this;
    }

    /**
     * Easily set/replace the complete row at an index.
     *
     * @throws IndexOutOfBoundsException
     */
    public Table row(int index, Row newRow) {
        Row oldRow = (Row) rows.children.get(index);
        rows.replace(oldRow, newRow);
        return this;
    }

    public Header getHeaderAt(int index){
        return (Header) headers.children.get(index);
    }

    public static class Headers extends Component<Headers> {
        /**
         * Reference to parent table if needed for method chaining.
         */
        public final Table t;
        public Event<Header> _onHeaderClick = new Event<>();

        public Headers(Table table) {
            super("headers");
            this.t = table;
            width("100%");
            Consumer<AddedChildEvent> superAdd = this._add;
            this._add = e -> {
                if(e.isFirstAdd && e.childComp instanceof Header){
                    e.childComp.onClick(click -> {
                        _onHeaderClick.execute((Header) e.childComp);
                    });
                }
                superAdd.accept(e);
            };
        }

        public Headers onHeaderClick(Consumer<Header> code){
            _onHeaderClick.addAction((event) -> code.accept(event));
            return this;
        }
    }

    public static class Rows extends Component<Rows> {
        /**
         * Reference to parent table if needed for method chaining.
         */
        public final Table t;
        public Event<Row> _onRowClick = new Event<>();

        public Rows(Table table) {
            super("rows");
            childVertical();
            this.t = table;
            width("100%"); // Children grow height of this layout
            Consumer<AddedChildEvent> superAdd = this._add;
            this._add = e -> {
                if(e.isFirstAdd && e.childComp instanceof Row){
                    e.childComp.onClick(click -> {
                        _onRowClick.execute((Row) e.childComp);
                    });
                }
                superAdd.accept(e);
            };
        }

        public Rows onRowClick(Consumer<Row> code){
            _onRowClick.addAction((event) -> code.accept(event));
            return this;
        }
    }

    public static class Row extends Component<Row> {
        public Row() {
            addClass("desku-table-row");
        }
    }

    public static class Header extends Component<Header> {
        /**
         * Width and height styles of this header are
         * also used for each of its rows.
         */
        private boolean isForwardSizeToRows = true;

        public Header() {
            addClass("desku-table-header");
        }

        public boolean isForwardSizeToRows(){
            return isForwardSizeToRows;
        }

        public Header forwardSizeToRows(boolean b){
            this.isForwardSizeToRows = b;
            return this;
        }

        public Component<?> getContent(){
            return children.get(0);
        }
    }
}
