package org.architecture;

import org.architecture.drawio.IconsHandler;
import org.architecture.drawio.MxLibrary;
import org.architecture.drawio.MxLibraryHandler;
import org.architecture.plantuml.IconToSpriteConverter;
import org.architecture.svg.NanoOptimizer;
import org.architecture.svg.SVGOptimizer;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class GeneralTester {
    public static void main(String[] args) throws IOException {
        testIconsHandler();
        testMxLibraryHandler();
        testIconToSpriteConverter();
        //testSVGOptimizer();
        //testNanoOptimizer();
    }

    public static void testIconsHandler() throws IOException {
        Path source = Paths.get("data/inputs/offline.html");
        Path target = Paths.get("data/outputs/offline/icons/");
        IconsHandler iconsHandler = new IconsHandler();
        Stream<Element> svgs = iconsHandler.parseIcons(source);
        iconsHandler.saveIcons(svgs, target);
    }

    public static void testMxLibraryHandler() throws IOException {
        Path source = Paths.get("data/inputs/online.html");
        Path target = Paths.get("data/outputs/online/icons");
        MxLibraryHandler handler = new MxLibraryHandler();
        Stream<MxLibrary> libraries = handler.parseLibraries(source);
        handler.saveIcons(libraries, target);
    }

    public static void testIconToSpriteConverter() {
        Path offlineIcons = Paths.get("data/outputs/offline/icons");
        Path offlineSprites = Paths.get("data/outputs/offline/sprites");
        Path onlineIcons = Paths.get("data/outputs/online/icons");
        Path onlineSprites = Paths.get("data/outputs/online/sprites");

        IconToSpriteConverter converter = new IconToSpriteConverter();
        //converter.convert(offlineIcons, offlineSprites);
        converter.convert(onlineIcons, onlineSprites);
    }
    
    public static void testSVGOptimizer() {
        Path icons = Paths.get("data/outputs/offline/icons");
        Path optimized = Paths.get("data/outputs/offline/svgo");

        SVGOptimizer svgo = new SVGOptimizer();
        svgo.optimize(icons, optimized);
    }

    public static void testNanoOptimizer() {
        Path icons = Paths.get("data/outputs/offline/icons");
        Path optimized = Paths.get("data/outputs/offline/nano");

        NanoOptimizer nano = new NanoOptimizer();
        nano.optimize(icons, optimized);
    }

}
