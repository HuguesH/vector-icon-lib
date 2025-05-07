package org.architecture.svg;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DefaultOptimizer implements Optimizer{
    private static final Logger logger = Logger.getLogger(DefaultOptimizer.class.getName());

    public DefaultOptimizer(){

    }

    @Override
    public Element optimize(Element svg){
        logger.fine("optimizing svg element limiting attribute set and compacting structure");
        return optimizeAttributes(optimizeTree(svg));
    }

    @Override
    public void optimize(Path source, Path target) {
        throw new UnsupportedOperationException();
    }

    protected Element optimizeAttributes(Element svg) {
        discardCustomAttributes(svg);

        Attribute width = svg.attribute("width");
        Attribute height = svg.attribute("height");
        Attribute viewBox = svg.attribute("viewBox");
        Map<String, String> styles = toMap(svg.attr("style"));

        svg.clearAttributes();
        svg.attr("xmlns", "http://www.w3.org/2000/svg");
        if(svg.selectFirst("[^xlink:]") != null){
            svg.attr("xmlns:xlink", "http://www.w3.org/1999/xlink");
        }

        if(width != null){
            svg.attr("width", correctDimension(width.getValue()));
        }
        else if(styles.containsKey("width")){
            svg.attr("width", correctDimension(styles.get("width")));
        }

        if(height != null){
            svg.attr("height", correctDimension(height.getValue()));
        }
        else if(styles.containsKey("height")){
            svg.attr("height", correctDimension(styles.get("height")));
        }

        if(viewBox != null){
            logger.fine("Correct already existing viewBox attribute");
            correctViewBox(viewBox);
            svg.attr("viewBox", viewBox.getValue());
        }
        else {
            String value = String.format("0 0 %s %s", svg.attr("width"), svg.attr("height"));
            svg.attr("viewBox", value);
            logger.fine(String.format("Append constructed attribute viewBox(%s)", value));
        }
        return svg;
    }

    protected Element optimizeTree(Element svg) {
        logger.fine("Discard extra directives (foreignObject, metadata, title, desc)");
        //discard unnecessary elements
        svg.select("foreignObject, metadata, title, desc").forEach(Node::remove);
        svg.select("*").forEach(element -> {
            logger.finer("Discard custom directives, having ':' in tag name)");
            //discard custom elements
            if(element.tagName().contains(":")){
                element.remove();
            }
        });

        logger.fine("Discard empty groups and compact non-empty groups");
        //discard empty groups and compact non-empty groups
        while ((svg.selectFirst("g:empty") != null) || (svg.selectFirst("g>g:only-child") != null)){
            svg.select("g:empty").forEach(Node::remove);
            Element onlyChild = svg.selectFirst("g>g:only-child");
            while (onlyChild != null){
                assert onlyChild.parent() != null;
                onlyChild.parent().replaceWith(onlyChild);
                onlyChild = svg.selectFirst("g>g:only-child");
            }
        }
        return svg;
    }

    protected void discardCustomAttributes(Element svg) {
        svg.select("*").forEach(element -> {
            Attributes origin = element.attributes().clone();
            element.clearAttributes();
            origin.forEach(attribute -> {
                if (attribute.getKey().equals("xlink:href")){//preserve internal reference
                    logger.finer(String.format("Preserve attribute %s=%s", attribute.getKey(), attribute.getValue()));
                    element.attr(attribute.getKey(), attribute.getValue());
                }
                else if (!(attribute.getKey().contains(":") || attribute.getKey().contains("-"))){   //discard custom attributes
                    logger.finer(String.format("Retain attribute %s=%s", attribute.getKey(), attribute.getValue()));
                    element.attr(attribute.getKey(), attribute.getValue());
                }
                else{
                    logger.finer(String.format("Discard attribute %s=%s", attribute.getKey(), attribute.getValue()));
                }
            });
        });
    }

    protected String correctDimension(String dimension){
        String correctedDimension = String.valueOf(Math.round(Float.parseFloat(dimension.replaceAll("[^\\d.]", ""))));
        logger.finer(String.format("%s -> %s", dimension, correctedDimension));
        return correctedDimension;
    }

    protected void correctViewBox(Attribute viewBox){
        String[] value = viewBox.getValue().split(" ");
        String[] correcteValue = value.clone();
        correcteValue[2] = correctDimension(value[2]);
        correcteValue[3] = correctDimension(value[3]);
        String[] concatenation = Stream.of(value, correcteValue).flatMap(Stream::of).toArray(String[]::new);
        logger.finer(String.format("viewBox=(%s %s %s %s) -> viewBox=(%s %s %s %s)", concatenation));
        viewBox.setValue(String.join(" ", correcteValue));
    }

    protected Map<String, String> toMap(String style){
        logger.finer(String.format("Convert style in key value pair :: %s", style));
        Map<String, String> map = new HashMap<>();
        for (String part : style.split(";")){
            String[] pair = part.split(":");
            if(pair.length == 2) {
                map.put(pair[0].trim(), pair[1].trim());
            }
        }
        return map;
    }
}
