import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class MultipartUtil {
    private static final String LINE_FEED = "\r\n";

    public static String getBody(String boundary, String formFieldName, String formFieldValue, String formFileName, File uploadFile) throws IOException {
        String body;

        body = "--" + boundary + LINE_FEED;
        body = body + "Content-Disposition: form-data; name=\"" + formFieldName + "\"" + LINE_FEED;
        body = body + LINE_FEED;
        body = body + formFieldValue + LINE_FEED;
        body = body + "--" + boundary + LINE_FEED;
        body = body + "Content-Disposition: form-data; name=\"" + formFileName + "\"; filename=\"" + uploadFile.getName() + "\"" + LINE_FEED;
        body = body + "Content-Type: " + URLConnection.guessContentTypeFromName(uploadFile.getName()) + LINE_FEED;
        body = body + LINE_FEED;
        body = body + FileUtils.readFileToString(uploadFile, StandardCharsets.UTF_8);
        body = body + LINE_FEED;
        body = body + "--" + boundary + "--" + LINE_FEED;

        return body;
    }

    private static String getBits(byte b) {
        String result = "";
        for (int i = 0; i < 8; i++)
            result += (b & (1 << i)) == 0 ? "0" : "1";
        return result;
    }
}
