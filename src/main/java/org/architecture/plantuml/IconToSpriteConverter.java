package org.architecture.plantuml;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IconToSpriteConverter {
    private static final Logger logger = Logger.getLogger(IconToSpriteConverter.class.getName());

    public IconToSpriteConverter(){

    }

    public void convert(Path icons, Path sprites){
        try (Stream<Path> pathStream = Files.walk(icons)){
            pathStream.filter(Files::isDirectory).forEach(directory -> convert(directory, icons, sprites));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void convert(Path directory, Path icons, Path sprites) {
        logger.info("Start building sprite for directory " + directory.toFile().getAbsolutePath());
        try (Stream<Path> pathStream = Files.list(directory)){
            String content = pathStream.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(f -> f.getName().endsWith(".svg"))
                    .map(this::toInlineSprite)
                    .collect(Collectors.joining("\n"));

            if(!content.isEmpty()){
                File pumlFile = getPumlFile(directory, icons, sprites);
                Files.createDirectories(pumlFile.toPath().getParent());
                try (PrintWriter pw = new PrintWriter(pumlFile)) {
                    pw.println("@startuml");
                    pw.println(content);
                    pw.println("@enduml");
                }
                logger.info("Created sprite " + pumlFile.getAbsolutePath());

                File pumlViewFile = new File(pumlFile.getParentFile(), pumlFile.getName().replace(".puml", "_view.puml"));
                try (PrintWriter pw = new PrintWriter(pumlViewFile)) {
                    pw.println("@startuml");
                    pw.print("!include ");
                    pw.println(pumlFile.getName());
                    pw.println("listsprites");
                    pw.println("@enduml");
                }
                logger.info("Created sprite view " + pumlViewFile.getAbsolutePath());
            }
        } catch (IOException e) {
            logger.severe("Error walking over " + directory.toFile().getAbsolutePath());
            throw new RuntimeException(e);
        }
        logger.info("End building sprite for directory " + directory.toFile().getAbsolutePath());
    }

    protected String toInlineSprite(File svg) {
        String name = toSpriteName(svg.getName().substring(0, svg.getName().lastIndexOf(".")));
        try {
            String content = new String(Files.readAllBytes(svg.toPath()));
            content = content.replace( " xmlns=\"http://www.w3.org/2000/svg\"", "");
            logger.info("sprite for " + svg.getAbsolutePath());
            return String.format("sprite %s %s", name, content);
        } catch (IOException e) {
            logger.severe("Error reading file " + svg.getAbsolutePath());
            throw new RuntimeException(e);
        }

    }

    protected File getPumlFile(Path directory, Path icons, Path sprites){
        File pumlFile = sprites.resolve(icons.relativize(directory)).toFile();
        pumlFile = Path.of(pumlFile.getParentFile().getAbsolutePath(), toSpriteName(pumlFile.getName()) + ".puml").toFile();
        logger.info(String.format("%s -> %s", directory.toFile().getAbsolutePath(), pumlFile.getAbsolutePath()));
        return pumlFile;
    }

    public String toSpriteName(String name){
        String spriteName = name.replaceAll("[)(&]", "")
                .replaceAll("[\\s-,\\/:?\"<>|.]+", "_")
                .replace("_$", "");
        logger.info(String.format("%s -> %s", name, spriteName));
        return spriteName;
    }
}
