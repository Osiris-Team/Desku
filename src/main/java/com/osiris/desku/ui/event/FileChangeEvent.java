package com.osiris.desku.ui.event;


import com.osiris.desku.ui.Component;

public class FileChangeEvent<T extends Component<?>> extends JavaScriptEvent<T> {
    /**
     * File name.
     */
    public final String value;
    /**
     * File name before change.
     */
    public final String valueBefore;
    /**
     * File content.
     */
    public final byte[] content;

    /**
     * @param rawJSMessage expected in this format: <br>
     *                     {"newValue": "...", "newContent", "...", "eventAsJson": {...}}
     * @param comp
     */
    public FileChangeEvent(String rawJSMessage, T comp, String valueBefore) {
        super(rawJSMessage, comp);
        this.value = jsMessage.get("newValue").getAsString();

        // Convert file content that is string binary to byte[]
        String content = jsMessage.get("newContent").getAsString();
        String[] bytes = content.split(" ");
        int length = bytes.length;
        byte[] byteArray = new byte[length];
        for (int i = 0; i < bytes.length; i++) {
            byteArray[i] = Byte.parseByte(bytes[i]);
        }
        this.content = byteArray;
        this.valueBefore = valueBefore;
    }

}
