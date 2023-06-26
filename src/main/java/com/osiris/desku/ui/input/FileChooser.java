package com.osiris.desku.ui.input;

import com.osiris.desku.App;
import com.osiris.desku.Icon;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Image;
import com.osiris.desku.ui.display.Table;
import com.osiris.desku.ui.display.Text;
import com.osiris.events.Event;
import com.osiris.jlib.logger.AL;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class FileChooser extends Component<FileChooser> {

    // Layout
    public TextField tfSelectedFiles;
    public DirectoryView directoryView = new DirectoryView(App.userDir.getAbsoluteFile());
    public Event<FileAsRow> _onFileSelected = new Event<>();
    public Event<FileAsRow> _onFileDeselected = new Event<>();
    private boolean isMultiSelect = true;


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
        this.tfSelectedFiles = new TextField(label, defaultValue);
        directoryView.visible(false);
        childVertical();
        tfSelectedFiles.onClick(e -> {
            directoryView.visible(!directoryView.isVisible());
        });
        onFileSelected(e -> {
            String filePaths = tfSelectedFiles.getValue();
            filePaths += e.cleanFilePath+"; ";
            this.tfSelectedFiles.setValue(filePaths);
        });
        onFileDeselected(e -> {
            String filePaths = tfSelectedFiles.getValue();
            filePaths = filePaths.replace(e.cleanFilePath+";", "");
            this.tfSelectedFiles.setValue(filePaths);
        });

        add(this.tfSelectedFiles, this.directoryView);
    }

    public FileChooser onFileSelected(Consumer<FileAsRow> code){
        _onFileSelected.addAction(file -> code.accept(file));
        return this;
    }
    public FileChooser onFileDeselected(Consumer<FileAsRow> code){
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
        return directoryView.dir;
    }

    public FileChooser setDir(File dir) {
        directoryView.setDir(dir);
        return this;
    }


    public class FileAsRow extends Table.Row{
        public File file;
        public String cleanFilePath;
        public DirectoryView directoryView;
        public CheckBox checkBox = new CheckBox().onValueChange(e -> {
            if(e.value && !isMultiSelect && !directoryView.selectedFiles.isEmpty()){
                e.comp.setValue(false);
                return;
            }
            if(e.value) {
                directoryView.selectedFiles.add(this);
                _onFileSelected.execute(this);
            } else{
                directoryView.selectedFiles.remove(this);
                _onFileDeselected.execute(this);
            }
        });
        public Image icon;
        public Text txtFileName;
        public Text txtLastModified;

        public FileAsRow(DirectoryView directoryView, File file, Image icon){
            this.directoryView = directoryView;
            this.file = file;
            this.cleanFilePath = file == null ? "" : file.getAbsolutePath().replace("\\", "/");
            add(checkBox);
            add(this.icon = icon);
            if(file == null){
                add(txtFileName = new Text("..DRIVES"));
                add(txtLastModified = new Text(""));
            } else{
                add(txtFileName = new Text(file.getName()));
                add(txtLastModified = new Text(new Date(file.lastModified()).toString()));

            }
            if(file==null || file.isDirectory()){
                onDoubleClick(e -> {
                    directoryView.setDir(file);
                });
            }
        }
    }

    public class DirectoryView extends Component<DirectoryView>{
        public Table table;
        public CopyOnWriteArrayList<FileAsRow> files = new CopyOnWriteArrayList<>();
        public CopyOnWriteArrayList<FileAsRow> selectedFiles = new CopyOnWriteArrayList<>();
        private File dir;

        public DirectoryView(String dir) {
            setDir(new File(dir));
        }
        public DirectoryView(File dir) {
            childVertical();
            setDir(dir);
        }

        public File getDir() {
            return dir;
        }

        public void setDir(File dir) {
            this.dir = dir;

            files.clear();
            removeAll();

            table = new Table();
            add(table);
            table.headers("Select", "Icon", dir == null ? ".." : // If this the case then parent shows drives/roots
                    dir.getAbsolutePath().replace("\\", "/"), "Modified");
            String selectWidth = "5%", iconWidth = "5%", nameWidth = "70%", modifiedWidth = "20%";
            table.getHeaderAt(0).width(selectWidth);
            table.getHeaderAt(1).width(iconWidth);
            table.getHeaderAt(2).width(nameWidth).childStart();
            table.getHeaderAt(3).width(modifiedWidth).childStart();

            try{
                List<File> _files = new ArrayList<>();
                if(dir != null){
                    // Add parent dir first, or drives view if parent dir is null
                    File parentDir = dir.getParentFile();
                    _files.add(parentDir);
                    // First half is directories, then actual files
                    for (File f : dir.listFiles()) {
                        if(f.isDirectory()) _files.add(f);
                    }
                    for (File f : dir.listFiles()) {
                        if(f.isFile()) _files.add(f);
                    }
                } else{
                    // show drives
                    Collections.addAll(_files, File.listRoots());
                }

                // Get default selected files
                List<File> defaultSelectedFiles = new ArrayList<>();
                for (String s : tfSelectedFiles.defaultValue.split(";")) {
                    if(!s.trim().isEmpty())
                        defaultSelectedFiles.add(new File(s));
                }

                // Create UI components for files
                for (File file : _files) {

                    FileAsRow fileAsRow = new FileAsRow(this, file,
                            (file == null || file.isDirectory() ? Icon.solid_folder() : Icon.regular_file()));

                    int iSelectedFile = -1;
                    for (int i = 0; i < selectedFiles.size(); i++) {
                        FileAsRow f = selectedFiles.get(i);
                        if (f.file.getAbsolutePath().equals(file.getAbsolutePath())) {
                            iSelectedFile = i;
                            //AL.info("Found already selected file! "+f.file);
                            break;
                        }
                    }
                    if(iSelectedFile != -1){
                        selectedFiles.set(iSelectedFile, fileAsRow);
                        fileAsRow.checkBox.setValue(true);
                    }

                    boolean isSelectedByDefault = false;
                    for (int i = 0; i < defaultSelectedFiles.size(); i++) {
                        File f = defaultSelectedFiles.get(i);
                        if (f.getAbsolutePath().equals(file.getAbsolutePath())) {
                            isSelectedByDefault = true;
                            //AL.info("Found DEFAULT selected file! "+f);
                            break;
                        }
                    }
                    if(isSelectedByDefault) fileAsRow.checkBox.setValue(true);
                    
                    files.add(fileAsRow);
                    table.row(fileAsRow);
                    fileAsRow.children.get(0).width(selectWidth);
                    fileAsRow.children.get(1).width(iconWidth);
                    fileAsRow.children.get(2).width(nameWidth).childStart();
                    fileAsRow.children.get(3).width(modifiedWidth).childStart();
                }
                if(dir != null){
                    // Since firstRow is always parent, also set its text to ..
                    FileAsRow firstRow = (FileAsRow) table.rows.children.get(0);
                    firstRow.txtFileName.set("..");
                }
            } catch (Exception e) {
                String msg = "Failed to retrieve directory content ("+e.getMessage()+") for " + dir;
                AL.warn(msg, e);
                table.row(msg, "", "", "");
            }
        }
    }

}
