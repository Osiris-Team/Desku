package com.osiris.desku.ui.input;

import com.osiris.desku.UI;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.FileChangeEvent;
import com.osiris.events.Event;

import java.util.function.Consumer;

public class FileChooser extends Component<FileChooser> {

    // Layout
    public Text label;
    public Input input = new Input("file");

    // Events
    public Event<FileChangeEvent<FileChooser>> _onValueChange = new Event<>();

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
        this.label = label;
        add(this.label, this.input);
        childVertical();
        this.input.putAttribute("value", defaultValue);
    }

    /**
     * The accept attribute value is a string that defines the file types the file input should accept.
     * This string is a comma-separated list of unique file type specifiers.
     * Because a given file type may be identified in more than one manner,
     * it's useful to provide a thorough set of type specifiers when you need files of a given format. <br>
     * Example: .doc,.docx,.xml,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document
     */
    public FileChooser accept(String s) {
        putAttribute("accept", s);
        return this;
    }

    public String getValue() {
        return this.input.element.attr("value");
    }

    /**
     * Triggers {@link #_onValueChange} event.
     */
    public FileChooser setValue(String s) {
        this.input.putAttribute("value", s);
        return this;
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, String, Consumer)
     */
    public FileChooser onValueChange(Consumer<FileChangeEvent<FileChooser>> code) {
        _onValueChange.addAction((event) -> code.accept(event));
        // TODO this is probably slow af, and reads the complete file twice into memory (on JS and Java side)
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
                    if (msg.equals("null")) return;
                    msg = msg.replace("\\", "/");
                    FileChangeEvent<FileChooser> e = new FileChangeEvent<>(msg, this, input.element.attr("value"));
                    input.element.attr("value", e.name); // Change in memory value, without triggering another change event
                    _onValueChange.execute(e); // Executes all listeners
                });
        return _this;
    }


}
