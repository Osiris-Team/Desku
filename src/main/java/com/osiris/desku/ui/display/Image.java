package com.osiris.desku.ui.display;


import com.osiris.desku.App;
import com.osiris.desku.ui.Component;
import com.osiris.jlib.logger.AL;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.nio.file.Files;

public class Image extends Component<Image> {
    /**
     * Java integration of the HTML img tag. <br>
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img">https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img</a>
     * <br><br>
     * If not existing the image will be written to<br>
     * {@link App#htmlDir}/java-images/IMAGE_NAME.
     *
     * @param image for example of type {@link BufferedImage}.
     * @param name for example "image.png" or "/image.jpg" or "/folder/image.jpg".
     */
    public Image(RenderedImage image, String name) {
        super("img");
        // Paths
        String src = name;
        if (!src.startsWith("/")) src = "/" + src;
        String packagePath = "/java-images";
        String imgNewPath = packagePath + src;
        File imgFile = new File(App.htmlDir + imgNewPath);

        // Set src of image
        String attrSrc = imgNewPath;
        if (attrSrc.startsWith("/")) attrSrc = attrSrc.replaceFirst("/", "");
        element.attr("src", attrSrc);

        // Create image file if needed otherwise use existing one
        if (imgFile.exists()) return;
        imgFile.getParentFile().mkdirs();
        try {
            String format = name.substring(name.lastIndexOf(".") + 1);
            if(!ImageIO.write(image, format, imgFile))
                throw new Exception("No writer for image of type \""+format+"\".");
            AL.info("Written java-image to: " + imgFile);
        } catch (Exception e) {
            AL.warn("Failed to write java-image ("+imgFile+").", e);
        }
    }

    /**
     * Java integration of the HTML img tag. <br>
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img">https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img</a>
     *
     * @param clazz image must be in the same package as this class or in a sub-package/sub-directory of this package.
     *              See {@link App#getResourceInPackage(Package, String)} for more details.
     * @param src   examples: "/image.png" or "image.png" or "/sub-dir/image.png" or "sub-dir/image.png"
     */
    public Image(Class<?> clazz, String src) {
        this("/" + clazz.getPackage().getName().replace(".", "/"), src);
    }

    /**
     * Java integration of the HTML img tag. <br>
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img">https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img</a>
     *
     * @param packagePath image must be in the same package as this class or in a sub-package/sub-directory of this package.
     *              See {@link App#getResourceInPackage(Package, String)} for more details.
     * @param src   examples: "/image.png" or "image.png" or "/sub-dir/image.png" or "sub-dir/image.png"
     */
    public Image(String packagePath, String src) {
        super("img");

        // Paths
        if (!src.startsWith("/")) src = "/" + src;
        String imgNewPath = (packagePath.equals("/") ? "" : packagePath)
                + src;
        File img = new File(App.htmlDir + imgNewPath);

        // Set src of image
        String attrSrc = imgNewPath;
        if (attrSrc.startsWith("/")) attrSrc = attrSrc.replaceFirst("/", "");
        element.attr("src", attrSrc);

        // Create image file if needed otherwise use existing one
        if (img.exists()) return;
        img.getParentFile().mkdirs();
        try {
            Files.copy(App.getResource(packagePath + src), img.toPath());
            //AL.info("Unpacked image to: " + img);
        } catch (Exception e) {
            AL.warn(e);
        }
    }
}
