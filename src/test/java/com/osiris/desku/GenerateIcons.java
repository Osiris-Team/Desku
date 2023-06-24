package com.osiris.desku;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

public class GenerateIcons {
    /**
     * Generates Icon class with free fontawesome 6.4.0 icons. <br>
     * Size around 6 MB.
     */
    @Test
    void generate() throws IOException {
        File dir = new File(System.getProperty("user.dir")+"/src/main/java");
        String readDirPackagePath = "/com/osiris/desku/ui/svg/fontawesome";
        File readDir = new File(dir + readDirPackagePath);

        // Setup class generator
        CompilationUnit iconsCU = new CompilationUnit("com.osiris.desku");
        File iconsFile = new File(dir+"/com/osiris/desku/Icon.java");
        ClassOrInterfaceDeclaration icons = iconsCU.setPackageDeclaration("com.osiris.desku").addClass("Icon")
                .setPublic(true).setJavadocComment("Automatically generated class. To re-generate/update <br>\n" +
                        "execute this in your console: ./gradlew build :test --tests \"com.osiris.desku.GenerateIcons\"");

        // Imports
        iconsCU.addImport("com.osiris.desku.ui.display.Image");

        AtomicInteger countStaticMethods = new AtomicInteger();
        for (File subDir : readDir.listFiles()) {
            if(subDir.isFile()) continue;
            String subDirName = subDir.getName().toLowerCase();
            for (File svg : subDir.listFiles()) {
                try{
                    if(svg.isDirectory()) continue;
                    String svgName = svg.getName().toLowerCase().replace("-", "_");
                    svgName = svgName.substring(0, svgName.lastIndexOf(".")).trim();
                    String methodName = subDirName +"_"+ svgName;
                    // Create static method
                    MethodDeclaration staticMethod = icons.addMethod(methodName)
                            .setType("Image") // Returns the file type
                            .setStatic(true).setPublic(true)
                            .setJavadocComment(new JavadocComment("<img src=\"https://raw.githubusercontent.com/FortAwesome/Font-Awesome/6.x/svgs/" +
                                    subDirName +"/"+svg.getName()+"\"> </img>"));
                    countStaticMethods.incrementAndGet();
                    //System.out.println("Constructor to static method with params: "+cparams);

                    String statement = "return new Image(\""+readDirPackagePath + "/" +subDirName+"\", \""+svg.getName()+"\")" +
                            ".addClass(\"icon\");";
                    staticMethod.setBody(new BlockStmt().addStatement(statement));
                } catch (Exception e) {
                    System.err.println("Error while processing: "+svg);
                    throw (e);
                }
            }
        }



        if(iconsFile.exists()) iconsFile.delete();
        iconsFile.getParentFile().mkdirs();
        iconsFile.createNewFile();
        System.out.println("Generating "+iconsFile);
        System.out.println("This might take a while...");
        AtomicInteger i = new AtomicInteger();
        iconsCU.findAll(MethodDeclaration.class).forEach(m -> {
            i.incrementAndGet();
        });

        Files.write(iconsFile.toPath(), iconsCU.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("Finished successfully! Generated/Updated "+countStaticMethods.get()+" static methods. ");
    }
}
