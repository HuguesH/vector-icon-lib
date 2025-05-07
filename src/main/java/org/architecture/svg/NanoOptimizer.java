package org.architecture.svg;

import org.architecture.Config;
import org.jsoup.nodes.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class NanoOptimizer implements Optimizer{
    public static String NANO_OPTIMIZER_PATH = "https://vecta.io/nano/api";// "https://api.vecta.io/nano/optimize";

    public NanoOptimizer() {
    }

    @Override
    public Element optimize(Element svg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void optimize(Path source, Path target) {
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if(file.toFile().getName().endsWith(".svg")){
                        Path optimized = target.resolve(source.relativize(file));
                        optimizeInternal(file, optimized);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    Path optimized = target.resolve(source.relativize(dir));
                    try {
                        Files.createDirectories(optimized);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.err.println("Error accessing file: " + file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void optimizeInternal(Path svg, Path optimized) {
        try {
            String content = callOptimize(svg).get();
            if (content != null) {
                write(optimized, content);
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected CompletableFuture<String> callOptimize(Path svg) throws IOException {
        HttpClient httpClient = HttpClient.newBuilder()
                .proxy(Config.proxySelector())
                .build();

        String requestBody = read(svg);
        HttpRequest request = HttpRequest.newBuilder(URI.create(NANO_OPTIMIZER_PATH))
                //.header("Content-Type", "text/plain")
                .header("Content-Type", "image/svg+xml")
                //.header("Accept", "image/svg+xml")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply( res ->{
                    System.out.printf("Http Status Code %d%n", res.statusCode());
                    return res;
                })
                .thenApply(HttpResponse::body);
    }

    protected String toBase64(Path svg){
        return String.format("data:image/png;base64,%s", new String(Base64.getEncoder().encode(read(svg).getBytes())));
    }

    protected String read(Path svg) {
        try {
            return new String(Files.readAllBytes(svg));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void write(Path svg, String content) {
        try (PrintWriter pw = new PrintWriter(svg.toFile())) {
            pw.print(content);
            pw.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
