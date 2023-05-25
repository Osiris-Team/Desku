package com.osiris.desku.ui.display;

import com.osiris.desku.ui.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Table with additional methods for reflection.
 */
public class RTable extends Component<RTable> {
    public Table table = new Table();
    public Class<?> clazz;
    public Predicate<Field> fieldPredicate = field -> {
        return !Modifier.isTransient(field.getModifiers());
    };

    /**
     * @param clazz uses this classes' fields for the headers.
     */
    public RTable(Class<?> clazz) {
        super("rtable");
        add(this.table);
        this.clazz = clazz;
        headers(clazz);
    }

    /**
     * @param clazz          uses this classes' fields for the headers.
     * @param fieldPredicate is used to determine which fields to use.
     */
    public RTable(Class<?> clazz, Predicate<Field> fieldPredicate) {
        super("rtable");
        add(this.table);
        this.clazz = clazz;
        this.fieldPredicate = fieldPredicate;
        headers(clazz, fieldPredicate);
    }


    /**
     * Easily set the fields of the provided class as headers. <br>
     * Transient fields are ignored.
     */
    public RTable headers(Class<?> clazz) {
        return headers(clazz, fieldPredicate);
    }

    /**
     * Easily set the fields of the provided class as headers. <br>
     */
    public RTable headers(Class<?> clazz, Predicate<Field> predicate) {
        table.headers.removeAll();
        for (Field f : clazz.getFields()) {
            if (predicate.test(f)) {
                String name = f.getName();
                table.headers.add(new Table.Header().add(new Text(name)));
            }
        }
        return this;
    }

    /**
     * @see #rows(Iterable, Predicate)
     */
    public <T> RTable rows(Iterable<T> rows) throws IllegalAccessException {
        return rows(rows, fieldPredicate);
    }

    /**
     * Removes current rows and creates news ones with the data contained in the provided list.
     *
     * @param rows list of objects where each object will fill a row with the values of its fields.
     *             The {@link Object#toString()} method will be used for each field
     *             (only if field is NOT null, otherwise an empty value will be added).
     *             Note that the objects
     *             must be the same type as {@link #clazz}
     *             to ensure that the data matches the headers.
     *             Also note that the {@link #fieldPredicate} must be the same one that was used for the headers.
     */
    public <T> RTable rows(Iterable<T> rows, Predicate<Field> predicate) throws IllegalAccessException {
        List<Field> fields = new ArrayList<>();
        for (Field f : clazz.getFields()) {
            if (predicate.test(f)) {
                f.setAccessible(true);
                fields.add(f);
            }
        }
        table.rows.removeAll();
        for (Object obj : rows) {
            Table.Row row = new Table.Row();
            for (Field f : fields) {
                Object fieldValue = f.get(obj);
                String val = fieldValue == null ? "" : fieldValue.toString();
                row.add(new Text(val));
            }
            table.rows.add(row);
        }
        return this;
    }

}
