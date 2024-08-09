package com.osiris.desku.ui.input.filechooser;

import com.osiris.desku.Icon;
import com.osiris.desku.ui.display.Image;
import com.osiris.desku.ui.display.Table;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.input.CheckBox;

import java.io.File;
import java.util.Date;

public class FileAsRow extends Table.Row {
    public File file;
    public String cleanFilePath;
    public DirectoryView directoryView;
    public CheckBox checkBox = new CheckBox();
    public Image icon;
    public Text txtFileName;
    public Text txtLastModified;

    public FileAsRow(FileChooser fileChooser, DirectoryView directoryView, File file) {
        this(fileChooser, directoryView, file,
                (file == null || file.isDirectory() ? Icon.solid_folder() : Icon.regular_file()));
    }

    public FileAsRow(FileChooser fileChooser, DirectoryView directoryView, File file, Image icon) {
        checkBox.readOnlyOnValueChange.addAction((action, e) -> {
            if (e.value && !fileChooser.isMultiSelect && !directoryView.selectedFiles.isEmpty()) {
                e.comp.setValue(false);
                action.skipNextActions();
            }
        }, Exception::printStackTrace);
        checkBox.onValueChange(e -> {
            if (e.value) {
                if(directoryView.selectedFiles.contains(this)) return;
                directoryView.selectedFiles.add(this);
                fileChooser._onFileSelected.execute(this);
            } else {
                directoryView.selectedFiles.remove(this);
                fileChooser._onFileDeselected.execute(this);
            }
        });
        this.directoryView = directoryView;
        this.file = file;
        this.cleanFilePath = file == null ? "" : file.getAbsolutePath().replace("\\", "/");
        add(checkBox);
        add(this.icon = icon);
        if (file == null) {
            add(txtFileName = new Text("..Drives"));
            add(txtLastModified = new Text(""));
        } else {
            add(txtFileName = new Text(file.getName().trim().isEmpty() ? cleanFilePath : file.getName())); // getName() is null for drives
            add(txtLastModified = new Text(new Date(file.lastModified()).toString()));

        }
        txtFileName.width("100%");
        if (file == null || file.isDirectory()) {
            txtFileName.onClick(e -> {
                directoryView.setDir(file);
            });
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileAsRow fileAsRow = (FileAsRow) o;
        return cleanFilePath.equals(fileAsRow.cleanFilePath);
    }

    @Override
    public int hashCode() {
        return cleanFilePath.hashCode();
    }
}
