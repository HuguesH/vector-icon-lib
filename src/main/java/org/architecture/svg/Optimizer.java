package org.architecture.svg;

import org.jsoup.nodes.Element;

import java.nio.file.Path;

public interface Optimizer {
    Element optimize(Element svg);
    void optimize(Path source, Path target);
}
