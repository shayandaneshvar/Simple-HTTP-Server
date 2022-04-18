package ir.shayandaneshvar.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class HttpRequest implements Serializable {
    private String method;
    private String version;
    private String path;
    private String host;
    private final List<String> headers = new LinkedList<>();

}
