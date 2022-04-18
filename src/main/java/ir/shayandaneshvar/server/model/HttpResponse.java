package ir.shayandaneshvar.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class HttpResponse implements Serializable {
    private String version;
    private String contentType;
    private String status;
    private byte[] content;

}
