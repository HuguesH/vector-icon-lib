package org.architecture.svg;

import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SVGOptimizer implements Optimizer{
    private static final Logger logger = Logger.getLogger(SVGOptimizer.class.getName());
    public static String SVGO_URL = "https://optimize.svgomg.net/";

    public SVGOptimizer() {
    }

    @Override
    public Element optimize(Element svg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void optimize(Path source, Path target) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("svgo", "-rf", source.toFile().getAbsolutePath(), "-o", target.toFile().getAbsolutePath());

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
            }

            int exitCode = process.waitFor();
            logger.info("Exited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "optimizeUsingNodeJSModule", e);
        }

    }


}
