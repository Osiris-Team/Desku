package com.osiris.desku;

import com.osiris.desku.ui.Component;
import com.osiris.jlib.UtilsFiles;
import com.osiris.jlib.logger.AL;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.ReentrantLock;

public class UI {
    /**
     * Last loaded html.
     */
    public final Route route;
    public final Component<?> content;

    public static volatile UI current = null;
    public static final ReentrantLock lock = new ReentrantLock();

    public UI(Route route) {
        lock.lock();
        current = this;
        this.route = route;
        this.content = route.loadContent();
        current = null;
        lock.unlock();
    }

    /**
     * Creates a snapshot of the current UI (the full HTML page) <br>
     * and returns it as {@link Document} for further processing. <br>
     * Note that changes to it won't be reflected in the actual UI.
     */
    public Document getSnapshot() {
        Document html = route.getDocument();
        Element outlet = html.getElementById("outlet");
        content.updateAll();
        outlet.appendChild(content.element);
        return html;
    }

    public File getDir(){
        // TODO in testing this resolves to the same directory after restarting even though it should be a new one
        // this results in cached files in that directory like images not getting updated, if changed.
        File dir = new File(App.tempDir + "/" + Integer.toHexString(hashCode()));
        dir.mkdirs();
        return dir;
    }

    /**
     * @see #snapshotToTempFile(Document)
     */
    public File snapshotToTempFile() throws IOException {
        return snapshotToTempFile(null);
    }

    /**
     * Creates a snapshot and writes the HTML to a file in the temp folder of the current user. <br>
     * If the path for this route is "/persons/john" for example then the html file will be created at: <br>
     * {@link App#tempDir}/{@link #hashCode()}/persons/john.html <br>
     * Note that the hash code is in hex format.
     *
     * @return the generated html file.
     */
    public File snapshotToTempFile(Document snapshot) throws IOException {
        File file = new File(getDir()
                + (route.path.equals("/") ? "/root.html" : (route.path + ".html")));
        if (snapshot == null) snapshot = getSnapshot();

        // Create symbolic link in current folder to global-styles.css
        // Symbolic links can't be created in temp folder?
        File link = new File(file.getParentFile() + "/" + App.styles.getName());
        AL.info("Copy of global styles: " + link);
        synchronized (App.styles) {
            if (!link.exists()) {
                link.getParentFile().mkdirs();
                link.createNewFile();
            }
            Files.copy(App.styles.toPath(), link.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        Element elLink = new Element("link");
        elLink.attr("rel", "stylesheet");
        elLink.attr("href", App.styles.getName());
        snapshot.getElementsByTag("head").get(0).appendChild(elLink);

        // Write html to temp file
        AL.info("Generate: " + file);
        file.getParentFile().mkdirs();
        if (!file.exists()) file.createNewFile();
        Files.write(file.toPath(), snapshot.outerHtml().getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
