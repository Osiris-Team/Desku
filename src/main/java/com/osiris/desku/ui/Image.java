package com.osiris.desku.ui;


import com.osiris.desku.App;
import com.osiris.desku.UI;
import com.osiris.jlib.logger.AL;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Image extends Component<Image> {
    public Image(Class<?> clazz, String src) {
        init(this, "img");

        // Set src of image
        String attrSrc = src;
        if(attrSrc.startsWith("/")) attrSrc = attrSrc.replaceFirst("/", "");
        element.attr("src", attrSrc);

        // Copy image if necessary
        File img = new File(UI.current.getDir() + (src.startsWith("/") ? src : ("/" + src)));
        if(img.exists()) return;
        img.getParentFile().mkdirs();
        try{
            Files.copy(App.getResourceInPackage(clazz.getPackage(), src), img.toPath());
            AL.info("Unpacked image to: "+img);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
