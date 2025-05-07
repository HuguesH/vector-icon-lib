package org.architecture.drawio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.architecture.Config;
import org.architecture.svg.DefaultOptimizer;
import org.architecture.svg.Optimizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.stream.Stream;

public class MxLibraryHandler {

    private Optimizer optimizer = new DefaultOptimizer();

    public MxLibraryHandler(){

    }

    public Optimizer getOptimizer() {
        return optimizer;
    }

    public void setOptimizer(Optimizer optimizer) {
        this.optimizer = optimizer;
    }

    public Stream<MxLibrary> parseLibraries(Path source) throws IOException {
        Document doc = Jsoup.parse(source);
        Element sidebar = doc.getElementsByClass("geSidebarContainer").first();
        assert sidebar != null;
        return sidebar.select("a[title]:not(:first-child)")
                .stream()
                .filter(this::accept)
                .map(this::parseLibrary);

    }

    public void saveIcons(Stream<MxLibrary> libraries, Path target) {
        libraries.forEach(library -> saveIcons(library, target));
    }

    protected void saveIcons(MxLibrary library, Path target){
        Path folder = Paths.get(target.toFile().getAbsolutePath(), library.getName());
        try {
            for (MxIcon icon : library.getIcons()){
                if(icon.isSvgImage()) {
                    Files.createDirectories(folder);
                    Path file = Paths.get(folder.toAbsolutePath().toString(), icon.getTitle() + ".svg");
                    Document doc = Jsoup.parse(icon.getSvgPayload(), Parser.xmlParser());
                    Element svg = doc.selectFirst("svg");
                    svg = optimizer.optimize(svg);
                    Files.writeString(file,
                            svg.toString().replaceAll(">\\s+<", "><").replace("viewbox", "viewBox"),
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean accept(Element libraryAnchor){
        String title = libraryAnchor.attr("title");
        String[] values = title.split("\r\n");
        return values.length > 1;
    }

    protected MxLibrary parseLibrary(Element libraryAnchor){
        String name = Objects.requireNonNull(libraryAnchor.selectFirst("span")).text();
        String title = libraryAnchor.attr("title");
        String[] values = title.split("\r\n");
        String url = java.net.URLDecoder.decode(values[1].substring(1), StandardCharsets.UTF_8);
        HttpClient httpClient = HttpClient.newBuilder()
                .proxy(Config.proxySelector())
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                String responseBody = new String(response.body());
                Document doc = Jsoup.parse(responseBody, Parser.xmlParser());
                Element element = doc.selectFirst("mxlibrary");
                Gson gson = new GsonBuilder().create();
                MxIcon[] icons = gson.fromJson(element.text(), MxIcon[].class);
                MxLibrary library = new MxLibrary();
                library.setName(name);
                library.setIcons(icons);
                return library;
                //Path file = Paths.get(target.toString(), name + ".xml");
                //Files.write(file, responseBody);
            }
            else{
                return null;
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
