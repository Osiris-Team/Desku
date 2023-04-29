package com.osiris.desku.ui.display;


import com.osiris.desku.App;
import com.osiris.desku.UI;
import com.osiris.desku.ui.Component;
import com.osiris.jlib.logger.AL;

import java.io.File;
import java.nio.file.Files;

public class Image extends Component<Image> {
    public Image(Class<?> clazz, String src) {
        init(this, "img");

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
