package com.osiris.desku.ui.input.fileuploader;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ValueChangeEvent;
import com.osiris.desku.ui.input.Input;

import java.util.function.Consumer;

/**
 * Component to let the user upload a file from their device.
 */
public class FileUploader extends Component<FileUploader, File> {

    // Layout
    public Text label;
    public Input<String> input;

    public FileUploader() {
        this("", "");
    }

    public FileUploader(String label) {
        this(label, "");
    }

    public FileUploader(String label, String defaultValue) {
        this(new Text(label).sizeS(), defaultValue);
    }

    public FileUploader(Text label, String defaultFileName) {
        super(new File("", new byte[]{}));
        this.label = label;
        this.input = new Input<>("file", defaultFileName);
        add(this.label, this.input);
        childVertical();
    }

    /**
     * The accept attribute value is a string that defines the file types the file input should accept.
     * This string is a comma-separated list of unique file type specifiers.
     * Because a given file type may be identified in more than one manner,
     * it's useful to provide a thorough set of type specifiers when you need files of a given format. <br>
     * Example: .doc,.docx,.xml,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document
     */
    public FileUploader accept(String s) {
        putAttribute("accept", s);
        return this;
    }

    /**
     * Returns the uploaded file, or a file where {@link File#content} length is 0, if nothing uploaded yet.
     */
    @Override
    public FileUploader getValue(Consumer<File> v) {
        return super.getValue(v);
    }


    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, String, Consumer)
     */

    public FileUploader onValueChange(Consumer<ValueChangeEvent<FileUploader, File>> code) {
        readOnlyOnValueChange.addAction((event) -> code.accept(event));
        // TODO this is probably slow af, and reads the complete file twice into memory (on JS and Java side)
        // This makes currently less sense, because Desku is two in one (server and client)
        // but if its someday distributed as a website/server then the above comment can be removed.
        UI.get().registerJSListener("input", input, "var fileContent = null;\n" +
                        "try{fileContent = event.target.fileContent;}catch(e){}\n" +
                        "if(fileContent == null){\n" +
                        "  message = `null`\n" +
                        "  var file = event.target.files[0]; \n" +
                        "  var reader = new FileReader();\n" +
                        "  event.target.reader = reader;\n" +
                        "  reader.onload = () => {\n" +
                        "    var string = '';\n" +
                        "    new Int8Array(reader.result).forEach(e =>{ string += e + ' ' });\n" +
                        "    event.target.fileContent = string;\n" +
                        "    event.target.dispatchEvent(event);\n" + // Trigger the same event again, but this time with the file content
                        "    event.target.fileContent = null;\n" +
                        "  }\n" +
                        "  reader.readAsArrayBuffer(file);\n" +
                        "}else {\n" +
                        "var fileName = event.target.value\n" +
                        "if(fileName.includes('\\\\')) fileName = fileName.split('\\\\').pop();\n" +
                        "else if(fileName.includes('/')) fileName = fileName.split('/').pop();\n" +
                        "  message = `{\"newValue\": \"` + fileName + `\", \"newContent\": \"` + event.target.fileContent + `\", \"eventAsJson\":` + message + `}`;\n" +
                        "}\n",
                (msg) -> {
                    if (msg.isEmpty()) return;
                    msg = msg.replace("\\", "/");
                    ValueChangeEvent<FileUploader, File> e = new ValueChangeEvent<>(msg, this, internalValue);
                    // Convert file content that is string binary to byte[] // TODO is this really necessary?
                    String content = e.jsMessage.get("newContent").getAsString();
                    String[] bytes = content.split(" ");
                    int length = bytes.length;
                    byte[] byteArray = new byte[length];
                    for (int i = 0; i < bytes.length; i++) {
                        byteArray[i] = Byte.parseByte(bytes[i]);
                    }
                    e.value.content = byteArray;
                    File newValue = e.value; // msg contains the new data and is parsed above in the event constructor
                    internalValue = newValue; // Change in memory value, without triggering another change event
                    element.attr("value", e.jsMessage.get("newValue").getAsString());
                    readOnlyOnValueChange.execute(e); // Executes all listeners
                });
        return _this;
    }


}
