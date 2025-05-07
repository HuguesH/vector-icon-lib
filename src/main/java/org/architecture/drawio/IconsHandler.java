package org.architecture.drawio;

import org.architecture.Config;
import org.architecture.svg.DefaultOptimizer;
import org.architecture.svg.Optimizer;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IconsHandler {

    private static final Logger logger = Logger.getLogger(IconsHandler.class.getName());

    public String DATA_IMAGE_PREFIX = "data:image/svg+xml;base64,";

    private Optimizer optimizer = new DefaultOptimizer();

    public IconsHandler(){
    }

    public Optimizer getOptimizer() {
        return optimizer;
    }

    public void setOptimizer(Optimizer optimizer) {
        this.optimizer = optimizer;
    }

    public Stream<Element> parseIcons(Path source) throws IOException {
        logger.info(String.format("Parse file %s", source));
        Document doc = Jsoup.parse(source);
        Element sidebar = doc.getElementsByClass("geSidebarContainer").first();
        assert sidebar != null;
        return sidebar.select("div.geSidebar>a.geItem>svg").stream()
                .map(this::loadImage)
                .map(this::optimize);
    }

    public void saveIcons(Stream<Element> svgs, Path target) {
        logger.info(String.format("save icons in %s", target));
        svgs.forEach(svg -> {
            if(accept(svg)){
                Path path = Paths.get(buildFilename(svg, target));
                try {
                    saveIcon(svg, path);
                } catch (IOException e) {
                    logger.severe(String.format("Could not save file %s", path.toString()));
                    //throw new RuntimeException(e);
                }
            }
        });
    }

    protected boolean accept(Element svg){
        String path = buildPath(svg);
        boolean accepted = path.startsWith("Office") || path.startsWith("AWS");
        logger.fine(String.format("Icons palette '%s' %s", path, (accepted ? "accepted" : "rejected")));
        return accepted;
    }

    public void saveIcon(Element svg, Path path) throws IOException {
        if(!Files.exists(path.getParent())){
            logger.fine(String.format("create directory %s", path.getParent().toString()));
            Files.createDirectories(path.getParent());
        }
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
             PrintWriter pw = new PrintWriter(bufferedWriter))
        {
            pw.print(svg.toString().replaceAll(">\\s+<", "><").replace("viewbox", "viewBox"));
            //pw.print(svg);
            pw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Element loadImage(Element svg) {
        Element image = svg.selectFirst("g>image[xlink:href]");
        if(image != null){
            logger.fine("Resolve referenced image");
            Element linkedSvg = loadLinkedImage(image.attr("xlink:href"));
            if(linkedSvg != null){
                svg.replaceWith(linkedSvg);
                return loadImage(linkedSvg);
            }
        }
        return svg;
    }

    public Element loadLinkedImage(String href) {
        if(href.startsWith(DATA_IMAGE_PREFIX)){
            logger.fine(String.format("Nested Base64 image resolution: %s", href));
            String image = new String(Base64.getDecoder().decode(href.substring(DATA_IMAGE_PREFIX.length())));
            Document doc = Jsoup.parse(image, Parser.xmlParser());
            return doc.selectFirst("svg");
        } else if (href.endsWith(".svg")) {
            logger.fine(String.format("Resolve external reference image: %s", href));
            try {
                Connection connection = Jsoup.connect(href);
                connection.proxy(Config.proxy());
                Document doc = connection.get();
                return doc.selectFirst("svg");
            } catch (IOException e) {
                logger.severe(String.format("Ressource Not Found: %s%n", href));
                //throw new RuntimeException(e);
            }
        }
        return null;
    }

    protected Element optimize(Element svg){
        return optimizer.optimize(svg);
    }

    protected String buildFilename(Element svg, Path target){
        String path = buildPath(svg);
        String iconName = buildIconName(svg);
        String fileName = String.format("%s/%s/%s.svg", target.toString(), path, iconName);
        logger.fine(fileName);
        return fileName;
    }

    private String buildPath(Element svg) {
        //Element libraryAnchor = Objects.requireNonNull(Objects.requireNonNull(svg.closest("div.geSidebar")).parent()).previousElementSibling();
        Element sidebar = svg.closest("div.geSidebar");
        if(sidebar == null){
            logger.severe(svg.outerHtml());
        }
        Element libraryAnchor = sidebar.parent().previousElementSibling();
        assert libraryAnchor != null;
        Element libraryDescriptionSpan = libraryAnchor.selectFirst("span");
        assert libraryDescriptionSpan != null;
        logger.fine(String.format("Found icons palette description %s", libraryDescriptionSpan.text()));
        return toPath(libraryDescriptionSpan.text());
    }

    private String buildIconName(Element svg) {
        Element descriptionDiv = svg.nextElementSibling();
        if(descriptionDiv == null){
            assert svg.parent() != null;
            descriptionDiv = svg.parent().selectFirst("div:matchesOwn(.+)");
        }
        return (descriptionDiv != null) ? toIconName(descriptionDiv.text()) : null;
    }

    protected String toPath(String libraryName){
        String path = Arrays.stream(libraryName.trim().split("/"))
                .map(String::trim)
                .collect(Collectors.joining("/"));
        logger.fine(String.format("Icon palette description to Path: %s -> %s", libraryName, path));
        return path;
    }

    protected String toIconName(String description){
        String iconName = description.trim()
                .replaceAll("[)(&]", "")
                .replaceAll("[\\s-,/\\:?\"<>|.]+", "_")
                .replace("_$", "");
        logger.fine(String.format("Icon description to icon name :%s -> %s", description, iconName));
        return iconName;
    }
}
