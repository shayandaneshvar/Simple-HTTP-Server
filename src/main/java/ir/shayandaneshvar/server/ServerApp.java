package ir.shayandaneshvar.server;

import ir.shayandaneshvar.server.integration.WebServer;

import java.io.IOException;

public class ServerApp {
    public static void main(String[] args) throws IOException {
        WebServer webServer = new WebServer(8192);
        new Thread(webServer).start();
    }
}
