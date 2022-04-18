package ir.shayandaneshvar.client.presenter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainController {
    @FXML
    private WebView webView;

    @FXML
    private TextField searchBar;

    @FXML
    private Button searchButton;

    @FXML
    private CheckBox rawCheckBox;

    @FXML
    private TextArea resultArea;

    @FXML
    void clickSearchButton(MouseEvent event) throws IOException {
        String path = searchBar.getText();
        if (path.startsWith("http://")) {
            path = path.substring(7);
            System.out.println(path);
        }
        String contextPath = "/";
        if (path.contains("/")) {
            contextPath = path.substring(path.indexOf("/"));
            path = path.substring(0, path.indexOf("/"));
            System.out.println(contextPath);
        }
        int port = 80;
        if (path.contains(":")) {
            port = Integer.parseInt(path.substring(path.indexOf(":") + 1));
            path = path.substring(0, path.indexOf(":"));
            System.out.println(path);
            System.out.println(port);
        }
        Socket socket = new Socket(path, port);
        OutputStream output = socket.getOutputStream();
        output.write(("GET " + contextPath + " HTTP/1.1 \r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Host: " + path + "\r\n").getBytes());
        output.write(("Connection: keep-alive\r\n").getBytes());
        output.write("\r\n".getBytes());
        output.write("\r\n\r\n".getBytes());
        output.flush();
        Pair<String, String> pair = readInputStream(socket.getInputStream());
        String content = pair.getValue();
        String header = pair.getKey();

        resultArea.setText("Headers:\n" + header + "\n Body: \n" + content);
        System.out.println(header.substring(header.indexOf("ContentType: ") + 13).trim());
        webView.getEngine().loadContent(content, header.substring(header.indexOf("ContentType: ") + 13).trim());
        socket.close();
    }

    public Pair<String, String> readInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder headerBuilder = new StringBuilder();
        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            headerBuilder.append(line).append("\r\n");
        }
        StringBuilder responseBuilder = new StringBuilder();
        reader.readLine();
        while (!(line = reader.readLine()).isEmpty()) {
            responseBuilder.append(line).append("\r\n");
        }
        System.out.println("Response: -------------------------------");
        System.out.println(responseBuilder);
        System.out.println("------------------------ End of Response.");
        return new Pair<>(headerBuilder.toString(), responseBuilder.toString());
    }

    @FXML
    void rawCheckBox(ActionEvent event) {
        resultArea.setVisible(rawCheckBox.isSelected());
        webView.setVisible(!rawCheckBox.isSelected());
    }

}
