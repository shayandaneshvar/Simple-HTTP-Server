package ir.shayandaneshvar.server.integration;

import ir.shayandaneshvar.server.model.HttpRequest;
import ir.shayandaneshvar.server.model.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class WebServer implements Runnable {
    private final int port;
    private volatile boolean stopped = false;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void stop() {
        stopped = true;
    }

    @Override
    public void run() {
        System.out.println("Starting Web Server ....");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port " + port + " ....");
            while (!stopped) {
                Socket client = serverSocket.accept();
                System.out.println("Accepting Client:" + client.toString());
                executor.execute(() -> handleClient(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Closing Web Server ...");
        }
    }

    @SneakyThrows
    private void handleClient(Socket client) {
        String request = readInputStream(client.getInputStream());

        String[] lines = request.split("\r\n");
        HttpRequest httpRequest = new HttpRequest()
                .setMethod(lines[0].split(" ")[0])
                .setPath(lines[0].split(" ")[1])
                .setVersion(lines[0].split(" ")[2]);

        for (int i = 2; i < lines.length; i++) {
            httpRequest.getHeaders().add(lines[i]);
        }
        System.out.println(httpRequest);

        HttpResponse response = dispatch(httpRequest);
        System.out.println(response);
        sendResponse(response, client.getOutputStream());

        client.close();
    }

    private void sendResponse(HttpResponse response, OutputStream output) throws IOException {
        if (response == null) {
            response = new HttpResponse().setStatus("404 Not Found")
                    .setContent("Oops!!!".getBytes(StandardCharsets.UTF_8))
                    .setVersion("1.1").setContentType("text/html");
        }
        output.write((response.getVersion() + " \r\n" +
                response.getStatus() + " \r\n").getBytes());
        output.write(("ContentType: " + response.getContentType() + "\r\n").getBytes());
        output.write("\r\n".getBytes());
        output.write(response.getContent());
        output.write("\r\n\r\n".getBytes());
        output.flush();
    }

    private HttpResponse dispatch(HttpRequest request) {
        HttpResponse response = new HttpResponse()
                .setVersion(request.getVersion());
        switch (request.getPath()) {
            case "/":
            case "/index":
                return dispatch(request.setPath("/index.html"));
            case "/info":
                return response
                        .setStatus("200 OK")
                        .setContentType("text/html")
                        .setContent("<p> Simple Http Server By Shayan Daneshvar </p>".getBytes(StandardCharsets.UTF_8));
            default:
                if (request.getPath().endsWith(".html")) {
                    return response.setStatus("200 OK")
                            .setContentType("text/html")
                            .setContent(getTemplate(request.getPath()));
                }
                if (request.getPath().contains(".")) {
                    try {
                        Path path = new File(getClass().getResource(request.getPath()).toURI().getPath()).toPath();
                        String type = Files.probeContentType(path);
                        System.err.println(type);
                        return response.setStatus("200 OK")
                                .setContentType(type)
                                .setContent(Files.readAllBytes(path));
                    } catch (Exception e) {
                        System.out.println("Something went wrong");
                    }
                }
                return null;
        }
    }

    private byte[] getTemplate(String path) {
        try {
            return Files.readAllBytes(new File(getClass().getResource(path).toURI().getPath()).toPath());
        } catch (Exception e) {
            return "Under Construction!".getBytes(StandardCharsets.UTF_8);
        }
    }

    public String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder requestBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            requestBuilder.append(line).append("\r\n");
        }
        System.out.println("Request: -------------------------------");
        System.out.println(requestBuilder);
        System.out.println("------------------------ End of Request.");
        return requestBuilder.toString();
    }

}
