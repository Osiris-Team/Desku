package com.osiris.desku.ui.display;

import com.osiris.desku.App;
import com.osiris.desku.ui.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Table extends Component<Table> {
    static {
        App.appendToGlobalStyles("table,\n" +
                "td {\n" +
                "    border: 1px solid #333;\n" +
                "}\n" +
                "\n" +
                "thead,\n" +
                "tfoot {\n" +
                "    background-color: #333;\n" +
                "    color: #fff;\n" +
                "}\n");
    }

    public HeaderContainer headers = new HeaderContainer(this);
    public RowContainer rows = new RowContainer(this);

    public Table() {
        init(this, "table");
        add(headers, rows);
        putStyle("display", "block"); // instead of table since that gives additional whitespace
    }

    /**
     * Easily set/replace the headers.
     */
    public Table headers(String... headers){
        this.headers.removeAll();
        for (String header : headers) {
            this.headers.add(new Header().add(new Text(header)));
        }
        return this;
    }

    /**
     * Easily set/replace the headers.
     */
    public Table headers(Header... headers){
        this.headers.removeAll();
        for (Header header : headers) {
            this.headers.add(header);
        }
        return this;
    }

    /**
     * Easily append a complete new row.
     */
    public Table row(String... data){
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
    public Table row(Row row){
        rows.add(row);
        return this;
    }

    /**
     * Easily set/replace the complete row at an index.
     * @throws IndexOutOfBoundsException
     */
    public Table row(int index, String... data){
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
     * @throws IndexOutOfBoundsException
     */
    public Table row(int index, Row newRow){
        Row oldRow = (Row) rows.children.get(index);
        rows.replace(oldRow, newRow);
        return this;
    }

    public static class HeaderContainer extends Component<HeaderContainer> {
        /**
         * Reference to parent table if needed for method chaining.
         */
        public final Table t;
        public final Row row = new Row();
        public HeaderContainer(Table table) {
            init(this, "thead");
            this.t = table;
            add(row);
            _add = row._add; // Add directly to this row
            _remove = row._remove; // Remove directly from this row
        }
    }

    public static class RowContainer extends Component<RowContainer> {
        /**
         * Reference to parent table if needed for method chaining.
         */
        public final Table t;
        public RowContainer(Table table) {
            init(this, "tbody");
            this.t = table;
            putStyle("display", "block");
        }
    }

    public static class Row extends Component<Row> {
        Consumer<AddedChildEvent> superAdd = _add;

        public Row() {
            init(this, "tr");
            _add = (e) -> { // event
                // Headers get added directly,
                // however other components first get wrapped into Table.Data
                if (e.childComp instanceof Header) superAdd.accept(e);
                else superAdd.accept(new AddedChildEvent(new Data().add(e.childComp), e.otherChildComp, e.isInsert, e.isReplace));
            };
        }
    }

    public static class Data extends Component<Data> {
        public Data() {
            init(this, "td");
        }
    }

    public static class Header extends Component<Header> {
        public Header() {
            init(this, "th");
        }
    }
}