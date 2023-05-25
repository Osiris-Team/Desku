package com.osiris.desku.ui.display;


import com.osiris.desku.App;
import com.osiris.desku.UI;
import com.osiris.desku.ui.Component;
import com.osiris.jlib.logger.AL;

import java.io.File;
import java.nio.file.Files;

public class Image extends Component<Image> {
    /**
     * Java integration of the HTML img tag. <br>
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img">https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img</a>
     *
     * @param clazz image must be in the same package as this class or in a sub-package/sub-directory of this package.
     *              See {@link App#getResourceInPackage(Package, String)} for more details.
     * @param src   examples: "/image.png" or "image.png" or "/sub-dir/image.png" or "sub-dir/image.png"
     */
    public Image(Class<?> clazz, String src) {
        super("img");

        // Paths
        String packagePath = "/" + clazz.getPackage().getName().replace(".", "/");
        String imgNewPath = (packagePath.equals("/") ? "" : packagePath)
                + (src.startsWith("/") ? src : ("/" + src));
        File img = new File(UI.get().getDir() + imgNewPath);

        // Set src of image
        String attrSrc = imgNewPath;
        if (attrSrc.startsWith("/")) attrSrc = attrSrc.replaceFirst("/", "");
        element.attr("src", attrSrc);

        // Create image file if needed otherwise use existing one
        if (img.exists()) return;
        img.getParentFile().mkdirs();
        try {
            Files.copy(App.getResourceInPackage(clazz.getPackage(), src), img.toPath());
            AL.info("Unpacked image to: " + img);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
