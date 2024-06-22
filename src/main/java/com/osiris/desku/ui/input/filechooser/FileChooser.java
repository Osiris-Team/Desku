package com.osiris.desku.ui.input.filechooser;

import com.osiris.desku.App;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.input.TextField;
import com.osiris.events.Event;

import java.io.File;
import java.util.function.Consumer;

public class FileChooser extends Component<FileChooser, String> {

    // Layout
    public TextField tfSelectedFiles;
    public DirectoryView directoryView;
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

    public FileChooser(Text label, String defaultValue) {
        super(defaultValue);
        this.tfSelectedFiles = new TextField(label, defaultValue);
        this.directoryView = new DirectoryView(this, App.userDir.getAbsoluteFile());
        directoryView.visible(false);
        childVertical();
        tfSelectedFiles.onClick(e -> {
            directoryView.visible(!directoryView.isVisible());
        });
        onFileSelected(e -> {
            String filePaths = tfSelectedFiles.getValue();
            filePaths += e.cleanFilePath + "; ";
            this.tfSelectedFiles.setValue(filePaths);
        });
        onFileDeselected(e -> {
            String filePaths = tfSelectedFiles.getValue();
            filePaths = filePaths.replace(e.cleanFilePath + ";", "");
            this.tfSelectedFiles.setValue(filePaths);
        });

        add(this.tfSelectedFiles, this.directoryView);
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
