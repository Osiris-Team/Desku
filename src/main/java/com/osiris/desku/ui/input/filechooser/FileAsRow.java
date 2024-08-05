package com.osiris.desku.ui.input.filechooser;

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

    public FileAsRow(FileChooser fileChooser, DirectoryView directoryView, File file, Image icon) {
        checkBox.readOnlyOnValueChange.addAction((action, e) -> {
            if (e.value && !fileChooser.isMultiSelect && !directoryView.selectedFiles.isEmpty()) {
                e.comp.setValue(false);
                action.skipNextActions();
            }
        }, Exception::printStackTrace);
        checkBox.onValueChange(e -> {
            if (e.value) {
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
            add(txtFileName = new Text("..DRIVES"));
            add(txtLastModified = new Text(""));
        } else {
            add(txtFileName = new Text(file.getName()));
            add(txtLastModified = new Text(new Date(file.lastModified()).toString()));

        }
        txtFileName.width("100%");
        if (file == null || file.isDirectory()) {
            txtFileName.onClick(e -> {
                directoryView.setDir(file);
            });
        }
    }
}
