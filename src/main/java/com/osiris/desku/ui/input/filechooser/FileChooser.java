package com.osiris.desku.ui.input.filechooser;

import com.osiris.desku.App;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ValueChangeEvent;
import com.osiris.desku.ui.input.Button;
import com.osiris.desku.ui.input.TextField;
import com.osiris.desku.ui.layout.Horizontal;
import com.osiris.events.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class FileChooser extends Component<FileChooser, String> {

    // Layout
    public Text label;
    public TextField tfSelectedFiles;
    public DirectoryView directoryView;
    public Horizontal btnsSelectedFiles;
    public Event<FileAsRow> _onFileSelected = new Event<>();
    public Event<FileAsRow> _onFileDeselected = new Event<>();
    public boolean isMultiSelect = true;


    public FileChooser() {
        this("", "");
    }

    public FileChooser(String label) {
        this(label, "");
    }

    public FileChooser(String label, String defaultValue) {
        this(new Text(label).sizeS(), defaultValue);
    }

    public FileChooser(String label, List<File> defaultValue) {
        this(new Text(label).sizeS(), pathsListToString(defaultValue));
    }

    public FileChooser(Text label, List<File> defaultValue) {
        this(label, pathsListToString(defaultValue));
    }

    public static String pathsListToString(List<File> list) {
        StringBuilder s = new StringBuilder();
        for (File file : list) {
            s.append(file.getAbsolutePath()).append(" ; ");
        }
        return s.toString();
    }

    public static List<File> stringToPathsList(String s) {
        List<File> l = new ArrayList<>();
        for (String path : s.split(";")) {
            path = path.trim();
            if(!path.isEmpty()) l.add(new File(path));
        }
        return l;
    }

    public FileChooser(Text label, String defaultValue) {
        super(defaultValue, String.class);
        this.childVertical().childGap(true);
        add(this.label = label);
        add(this.tfSelectedFiles = new TextField(label, defaultValue));
        add(this.btnsSelectedFiles = new Horizontal().padding(false).scrollable(true, "100%", "fit-content"));
        add(this.directoryView = new DirectoryView(this, App.userDir.getAbsoluteFile()));
        setValue(defaultValue);

        directoryView.visible(false);
        tfSelectedFiles.visible(false);
        tfSelectedFiles.onClick(e -> {
            directoryView.visible(!directoryView.isVisible());
        });
        onFileSelected(e -> {
            String filePaths = tfSelectedFiles.getValue();
            filePaths += e.cleanFilePath + " ; ";
            this.tfSelectedFiles.setValue(filePaths);
            btnsSelectedFiles.add(getButton(e));
        });
        onFileDeselected(e -> {
            String filePaths = tfSelectedFiles.getValue();
            filePaths = filePaths.replace(e.cleanFilePath + " ; ", "");
            this.tfSelectedFiles.setValue(filePaths);
            for (Component __ : btnsSelectedFiles.children) {
                Button btn = (Button) __;
                if(btn.label.getValue().equals(e.cleanFilePath))
                    btnsSelectedFiles.remove(btn);
            }
        });
    }

    private void setButtons(List<File> files){
        btnsSelectedFiles.removeAll();
        btnsSelectedFiles.add(new Button("Select File(s)").onClick(e -> {
            directoryView.visible(!directoryView.isVisible());
        }));
        for (File file : files) {
            btnsSelectedFiles.add(getButton(new FileAsRow(this, directoryView, file)));
        }
    }

    private Button getButton(FileAsRow e) {
        return new Button(e.cleanFilePath).onClick(e2 -> {
            setDir(e.file.isDirectory() ? e.file : e.file.getParentFile());
            directoryView.visible(!directoryView.isVisible());
        });
    }

    public FileChooser setValue(@Nullable File... v) {
        if(v == null) setValue("");
        else setValue(pathsListToString(Arrays.asList(v)));
        return this;
    }

    public FileChooser setValue(@Nullable List<File> v) {
        if(v == null) setValue("");
        else setValue(pathsListToString(v));
        return this;
    }

    @Override
    public FileChooser setValue(@Nullable String v) {
        tfSelectedFiles.setValue(v);
        setButtons(stringToPathsList(v));
        return this;
    }

    @Override
    public FileChooser getValue(Consumer<@NotNull String> v) {
        tfSelectedFiles.getValue(v);
        return this;
    }

    @Override
    public FileChooser onValueChange(Consumer<ValueChangeEvent<FileChooser, String>> code) {
        tfSelectedFiles.onValueChange(e -> {
            ValueChangeEvent<FileChooser, String> e2 = new ValueChangeEvent<>(e.messageRaw, e.message, this, e.value, e.valueBefore, e.isProgrammatic);
            code.accept(e2);
            String[] previousPaths = e.valueBefore.split(";");
            String[] paths = e.value.split(";");
            for (String path : paths) {
                path = path.trim();
                for (String previousPath : previousPaths) {
                    previousPath = previousPath.trim();
                    if(!path.equals(previousPath)){
                        // This is the path that was changed, thus deselect previous path
                        // This forces the user to use the UI instead of this text field to select the new path
                        if(!previousPath.isEmpty())
                        {
                            _onFileDeselected.execute(new FileAsRow(this, directoryView, new File(previousPath)));
                            _onFileSelected.execute(new FileAsRow(this, directoryView, new File(path)));
                        }
                    }
                }
            }
        });
        return this;
    }

    public FileChooser onFileSelectChange(Consumer<FileAsRow> code) {
        _onFileSelected.addAction(file -> code.accept(file));
        _onFileDeselected.addAction(file -> code.accept(file));
        return this;
    }

    public FileChooser onFileSelected(Consumer<FileAsRow> code) {
        _onFileSelected.addAction(file -> code.accept(file));
        return this;
    }

    public FileChooser onFileDeselected(Consumer<FileAsRow> code) {
        _onFileDeselected.addAction(file -> code.accept(file));
        return this;
    }

    /**
     * Allow or deny selection of multiple files.
     */
    public boolean isMultiSelect() {
        return isMultiSelect;
    }

    /**
     * Allow or deny selection of multiple files.
     */
    public FileChooser multiSelect(boolean b) {
        isMultiSelect = b;
        return this;
    }

    public File getDir() {
        return directoryView.getDir();
    }

    public FileChooser setDir(File dir) {
        directoryView.setDir(dir);
        return this;
    }
}
