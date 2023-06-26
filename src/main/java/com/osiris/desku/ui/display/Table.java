package com.osiris.desku.ui.display;

import com.osiris.desku.App;
import com.osiris.desku.ui.Component;
import com.osiris.events.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Table extends Component<Table> {
    static {
        try {
            App.appendToGlobalCSS(App.getCSS(Table.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Row headers = new Row().addClass("desku-table-header");
    public Rows rows = new Rows(this);
    /**
     * Gets recalculated in {@link #headers(Row...)}. <br>
     * Set to -1 or smaller, to disable.
     */
    public double maxColumnWidthPercent = 100;
    public void recalcMaxColumnWidthPercent(){
        if(maxColumnWidthPercent < 0) return;
        maxColumnWidthPercent = (1.0 / headers.children.size()) * 100.0;
        for (Component<?> header : headers.children) {
            header.putStyle("max-width", maxColumnWidthPercent+"%");
        }
        for (Component<?> row : rows.children) {
            for (Component<?> rowColumn : row.children.get(0).children) {
                rowColumn.putStyle("max-width", maxColumnWidthPercent+"%");
            }
        }
    }

    public Table() {
        add(headers, rows);
        addClass("desku-table");
        rows.onAddedChild.addAction(e -> {
           if(maxColumnWidthPercent > 0 && e.childComp instanceof Row){
               Row row = (Row) e.childComp;
               for (Component<?> rowColumn : row.children) {
                   rowColumn.putStyle("max-width", maxColumnWidthPercent+"%");
               }
           }
        });
        //putStyle("display", "block"); // instead of table since that gives additional whitespace
    }

    /**
     * Easily set/replace the headers.
     */
    public Table headers(String... headers) {
        this.headers.removeAll();
        for (String header : headers) {
            this.headers.add(new Text(header));
        }
        recalcMaxColumnWidthPercent();
        return _this;
    }

    /**
     * Easily set/replace the headers.
     */
    public Table headers(Component<?>... headers) {
        this.headers.removeAll();
        for (Component<?> header : headers) {
            this.headers.add(header);
        }
        recalcMaxColumnWidthPercent();
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

    public Component<?> getHeaderAt(int index){
        return headers.children.get(index);
    }

    public static class Headers extends Component<Headers> {
        /**
         * Reference to parent table if needed for method chaining.
         */
        public final Table t;
        public Event<Row> _onHeaderClick = new Event<>();

        public Headers(Table table) {
            super("headers");
            this.t = table;
            width("100%");
            Consumer<AddedChildEvent> superAdd = this._add;
            this._add = e -> {
                if(e.isFirstAdd && e.childComp instanceof Row){
                    e.childComp.onClick(click -> {
                        _onHeaderClick.execute((Row) e.childComp);
                    });
                }
                superAdd.accept(e);
            };
        }

        public Headers onHeaderClick(Consumer<Row> code){
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

            // Wrap all added children first into cell
            Consumer<AddedChildEvent> superAdd = _add;
            _add = e -> {
                Cell cell = new Cell();
                cell.add(e.childComp);
                e.childComp = cell;
                superAdd.accept(e);
            };
        }

        public List<Cell> getCells(){
            List<Cell> cells = new ArrayList<>();
            for (Component<?> child : children) {
                cells.add((Cell) child);
            }
            return cells;
        }

        public List<Component<?>> getContents(){
            List<Component<?>> contents = new ArrayList<>();
            for (Cell cell : getCells()) {
                contents.add(cell.getContent());
            }
            return contents;
        }
    }

    public static class Cell extends Component<Cell> {

        public Cell(){
            addClass("desku-table-cell");
        }

        public Component<?> getContent(){
            return children.get(0);
        }
    }
}
