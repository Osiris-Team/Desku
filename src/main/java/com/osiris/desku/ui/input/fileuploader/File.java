package com.osiris.desku.ui.input.fileuploader;

public class File {
    /**
     * File name.
     */
    public String name;
    /**
     * File content.
     */
    public byte[] content;

    public File(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

}
