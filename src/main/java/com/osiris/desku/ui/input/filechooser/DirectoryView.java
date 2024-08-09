package com.osiris.desku.ui.input.filechooser;

import com.osiris.desku.Icon;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Table;
import com.osiris.desku.ui.utils.NoValue;
import com.osiris.jlib.logger.AL;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DirectoryView extends Component<DirectoryView, NoValue> {
    FileChooser fileChooser;
    public Table table;
    public CopyOnWriteArrayList<FileAsRow> files = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<FileAsRow> selectedFiles = new CopyOnWriteArrayList<>();
    private File dir;

    public DirectoryView(FileChooser fileChooser, String dir) {
        super(NoValue.GET, NoValue.class);
        this.fileChooser = fileChooser;
        setDir(new File(dir));
    }

    public DirectoryView(FileChooser fileChooser, File dir) {
        super(NoValue.GET, NoValue.class);
        this.fileChooser = fileChooser;
        childVertical();
        setDir(dir);
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;

        fileChooser.tfSelectedFiles.onValueChange(e -> {
           // User or programmer changed the text field value containing all selected files
           // thus we update the selected files and this UI
            selectedFiles.clear();
            for (File file : FileChooser.stringToPathsList(e.value)) {
                selectedFiles.add(new FileAsRow(fileChooser, this, file));
            }
        });

        files.clear();
        removeAll();

        table = new Table();
        table.maxColumnWidthPercent = -1;
        add(table);
        table.headers("Select", "Icon", dir == null ? ".." : // If this the case then parent shows drives/roots
                dir.getAbsolutePath().replace("\\", "/"), "Modified");
        String selectWidth = "5%", iconWidth = "5%", nameWidth = "70%", modifiedWidth = "20%";
        table.getHeaderAt(0).width(selectWidth);
        table.getHeaderAt(1).width(iconWidth);
        table.getHeaderAt(2).width(nameWidth).childStart1();
        table.getHeaderAt(3).width(modifiedWidth).childStart1();

        try {
            List<File> _files = new ArrayList<>();
            if (dir != null) {
                // Add parent dir first, or drives view if parent dir is null
                File parentDir = dir.getParentFile(); // will be displayed with name ".."
                if(parentDir == null) Collections.addAll(_files, File.listRoots());
                else _files.add(parentDir);
                // First half is directories, then actual files
                File[] files1 = dir.listFiles();
                if(files1 != null && files1.length > 0){
                    for (File f : files1) {
                        if (f.isDirectory()) _files.add(f);
                    }
                    for (File f : files1) {
                        if (f.isFile()) _files.add(f);
                    }
                }
            } else {
                // show drives
                Collections.addAll(_files, File.listRoots());
            }

            // Get default selected files
            List<File> defaultSelectedFiles = new ArrayList<>();
            String defValue = fileChooser.tfSelectedFiles.getValue(); // falls back to default value if none
            for (String s : defValue.split(";")) {
                if (!s.trim().isEmpty())
                    defaultSelectedFiles.add(new File(s.trim()));
            }

            // Create UI components for files
            for (File file : _files) {

                FileAsRow fileAsRow = new FileAsRow(fileChooser, this, file);

                boolean isSelectedByDefault = false;
                for (int i = 0; i < defaultSelectedFiles.size(); i++) {
                    File f = defaultSelectedFiles.get(i);
                    if (f.getAbsolutePath().equals(file.getAbsolutePath())) {
                        isSelectedByDefault = true;
                        AL.info("Found DEFAULT selected file! "+f);
                        break;
                    }
                }
                if (isSelectedByDefault) fileAsRow.checkBox.setValue(true);

                files.add(fileAsRow);
                table.row(fileAsRow);
                fileAsRow.children.get(0).width(selectWidth);
                fileAsRow.children.get(1).width(iconWidth);
                fileAsRow.children.get(2).width(nameWidth).childStart1();
                fileAsRow.children.get(3).width(modifiedWidth).childStart1();
            }
            if (dir != null) {
                // Since firstRow is always parent, also set its text to ..
                FileAsRow firstRow = (FileAsRow) table.rows.children.get(0);
                firstRow.txtFileName.setValue("..");
            }
        } catch (Exception e) {
            String msg = "Failed to retrieve directory content (" + e.getMessage() + ") for " + dir;
            AL.warn(msg, e);
            table.row(msg, "", "", "");
        }
    }
}
