import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MultipartUtil {
    private static final String LINE_FEED = "\r\n";

    public static String getBody(String boundary, String formFieldName, String formFieldValue, String formFileName, String path) throws IOException {
        String body;

        File file = new File(path);

        body = "--" + boundary + LINE_FEED;
        body = body + "Content-Disposition: form-data; name=\"" + formFieldName + "\"" + LINE_FEED;
        body = body + LINE_FEED;
        body = body + formFieldValue + LINE_FEED;
        body = body + "--" + boundary + LINE_FEED;
        body = body + "Content-Disposition: form-data; name=\"" + formFileName + "\"; filename=\"" + file.getName() + "\"" + LINE_FEED;
        body = body + "Content-Type: image/*" + LINE_FEED;
        body = body + LINE_FEED;



        InputStream stream = new FileInputStream(file);
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer, Charset.defaultCharset());
        String theString = writer.toString();

        body = body + theString;


        body = body + LINE_FEED;
        body = body + "--" + boundary + "--" + LINE_FEED;

        return body;
    }
}
