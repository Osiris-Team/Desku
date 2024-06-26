package com.osiris.desku;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.osiris.desku.ui.Component;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class GenerateStatics {
    @Test
    void generate() throws IOException {
        File dir = new File(System.getProperty("user.dir")+"/src/main/java");

        CompilationUnit staticsCU = new CompilationUnit("com.osiris.desku");
        ClassOrInterfaceDeclaration statics = staticsCU.setPackageDeclaration("com.osiris.desku").addClass("Statics")
                .setPublic(true).setJavadocComment("Automatically generated class. To re-generate/update <br>\n" +
                        "execute this in your console: ./gradlew build :test --tests \"com.osiris.desku.GenerateStatics\"");
        List<String> methodNames = new ArrayList<>();

        AtomicInteger countStaticMethods = new AtomicInteger();
        FileUtils.iterateFiles(dir, null, true).forEachRemaining(f -> {
            try {
                if(!f.getName().endsWith(".java")) return;
                String fileNameWithoutExtension = f.getName().substring(0, f.getName().lastIndexOf("."));
                String name = f.getAbsolutePath().replace(dir.getAbsolutePath(), "");
                name = name.substring(0, name.lastIndexOf(".")) // Remove file extension
                        .replace("/", ".").replace("\\", ".");
                if(name.startsWith(".")) name = name.substring(1);
                Class<?> clazz = Class.forName(name);

                // Check if extends Desku Component
                CompilationUnit fileCU = StaticJavaParser.parse(f);
                Optional<ClassOrInterfaceDeclaration> _fileCLASS = fileCU.getClassByName(fileNameWithoutExtension);
                if(!_fileCLASS.isPresent()) {
                    System.out.println("POSSIBLE WARNING: File is named '"+fileNameWithoutExtension+"' but does not contain a single class declaration named like that.");
                    return;
                }
                ClassOrInterfaceDeclaration fileCLASS = _fileCLASS.get();
                boolean isComponent = isComponent(fileCU, fileCLASS);

                if(!isComponent) return;
                System.out.println("Found Desku Component: "+f);

                // Check for duplicate class names
                String methodName = fileNameWithoutExtension.toLowerCase();
                if (methodNames.contains(methodName))
                    throw new RuntimeException("There exist at least 2 components/classes named "+fileNameWithoutExtension+"!" +
                            " This is not allowed, rename your class!");
                methodNames.add(methodName);
                staticsCU.addImport(clazz);

                for (ImportDeclaration i : fileCU.getImports()) {
                    staticsCU.addImport(i);
                }


                fileCU.findAll(ConstructorDeclaration.class).stream()
                        .filter(c -> c.isPublic() && !c.isStatic())
                        .forEach(constructor -> {
                            String constructorName = constructor.getName().asString();
                            if(!constructorName.equals(fileNameWithoutExtension)) return; // Do not allow nested classes

                            // Create static method
                            StringBuilder sb = new StringBuilder();
                            NodeList<Parameter> cparams = new NodeList<>(constructor.getParameters());
                            MethodDeclaration staticMethod = statics.addMethod(methodName)
                                    .setType(fileNameWithoutExtension) // Returns the file type
                                    .setStatic(true).setPublic(true)
                                    .setJavadocComment(constructor.getJavadocComment().orElse(new JavadocComment("")))
                                    .setParameters(cparams);

                            // Handle generic types
                            NodeList<TypeParameter> typeParameters = fileCLASS.getTypeParameters();
                            if (!typeParameters.isEmpty()) {
                                staticMethod.setTypeParameters(typeParameters);
                            }

                            countStaticMethods.incrementAndGet();
                            //System.out.println("Constructor to static method with params: "+cparams);

                            // Create string: return new "ComponentName"(param1, param2, etc...)
                            for (Parameter param : cparams) {
                                sb.append(param.getName().asString()+", ");
                            }
                            String params = sb.toString();
                            if(!params.isEmpty()) {
                                params = params.substring(0, sb.lastIndexOf(","));
                            }
                            String statement = "return new "+constructorName+"("+params+");";
                            staticMethod.setBody(new BlockStmt().addStatement(statement));
                        });
            } catch (Exception e) {
                System.err.println("****\nFailed to process file due to an exception (see below). File: "+f+"\n****");
                e.printStackTrace();
            }
        });

        File staticsFile = new File(dir+"/com/osiris/desku/Statics.java");
        if(staticsFile.exists()) staticsFile.delete();
        staticsFile.getParentFile().mkdirs();
        staticsFile.createNewFile();
        System.out.println("Generating "+staticsFile);
        System.out.println("This might take a while...");
        AtomicInteger i = new AtomicInteger();
        staticsCU.findAll(MethodDeclaration.class).forEach(m -> {
            i.incrementAndGet();
        });
        Files.write(staticsFile.toPath(), staticsCU.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("Finished successfully! Generated/Updated "+countStaticMethods.get()+" static methods. ");
    }

    private boolean isComponent(CompilationUnit fileCU, ClassOrInterfaceDeclaration fileCLASS) {
        for (ClassOrInterfaceType extendedType : fileCLASS.getExtendedTypes()) {
            if (extendedType.getName().asString().equals(Component.class.getSimpleName())) {
                return true;
            }

            String extendedClassName = extendedType.asClassOrInterfaceType().getNameWithScope();
            Optional<ClassOrInterfaceDeclaration> extendedClass = findClassDeclaration(extendedClassName, fileCU);
            if (extendedClass.isPresent() && isComponent(fileCU, extendedClass.get())) {
                return true;
            }
        }
        return false;
    }

    private Optional<ClassOrInterfaceDeclaration> findClassDeclaration(String className, CompilationUnit currentCU) {
        // Check if class is in the current compilation unit
        Optional<ClassOrInterfaceDeclaration> classDecl = currentCU.getClassByName(className);
        if (classDecl.isPresent()) {
            return classDecl;
        }

        // Check if class is in imported packages
        for (ImportDeclaration importDecl : currentCU.getImports()) {
            String importName = importDecl.getNameAsString();
            if (importName.endsWith("." + className)) {
                try {
                    Path classPath = findFileForClass(importName);
                    if (classPath != null) {
                        CompilationUnit importedCU = StaticJavaParser.parse(classPath);
                        return importedCU.getClassByName(className);
                    }
                } catch (IOException e) {
                    System.err.println("Error parsing imported file: " + importName);
                    e.printStackTrace();
                }
            }
        }

        // Check if class is in the same package
        String packageName = currentCU.getPackageDeclaration().map(pd -> pd.getNameAsString() + ".").orElse("");
        try {
            Path classPath = findFileForClass(packageName + className);
            if (classPath != null) {
                CompilationUnit packageCU = StaticJavaParser.parse(classPath);
                return packageCU.getClassByName(className);
            }
        } catch (IOException e) {
            System.err.println("Error parsing file in same package: " + packageName + className);
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private Path findFileForClass(String className) {
        String classPath = className.replace('.', '/') + ".java";
        Path path = Paths.get(System.getProperty("user.dir"), "src/main/java", classPath);
        return Files.exists(path) ? path : null;
    }

}
