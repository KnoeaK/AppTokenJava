import com.fasterxml.jackson.databind.ObjectMapper;
import model.Applicant;
import model.DocSet;
import model.DocType;
import model.HttpMethod;
import model.IdDocSetType;
import model.RequiredIdDocs;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AppTokenJavaExample {
    // The description of the authorization method is available here: https://developers.sumsub.com/api-reference/#app-tokens
    private static final String SUMSUB_SECRET_KEY = "Z947czl1MjoJwj579y4adXPVUrFneM7y"; // Example: Hej2ch71kG2kTd1iIUDZFNsO5C1lh5Gq
    private static final String SUMSUB_APP_TOKEN = "tst:Xtf5G2mReRqNIVaGlQ7xapiv.WWM2nIxCh54Ku1g8v71axLRlM5CT2u4R"; // Example: tst:uY0CgwELmgUAEyl4hNWxLngb.0WSeQeiYny4WEqmAALEAiK2qTC96fBad
    private static final String SUMSUB_TEST_BASE_URL = "http://localhost:8090"; // Please don't forget to change when switching to production

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String applicantId = createApplicant();
        System.out.println("The applicant was successfully created: " + applicantId);

        addDocument(applicantId, "/Users/knoeak/MyProjects/AppTokenJava/src/main/resources/images/sumsub-logo.png");
    }

    public static String createApplicant() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        // https://developers.sumsub.com/api-reference/#creating-an-applicant


        // create entity
        DocSet identityDocSet = new DocSet(
                IdDocSetType.IDENTITY,
                Arrays.asList(DocType.PASSPORT, DocType.ID_CARD, DocType.DRIVERS)
        );

        DocSet selfieDocSet = new DocSet(
                IdDocSetType.SELFIE,
                Collections.singletonList(DocType.SELFIE)
        );

        Applicant applicant = new Applicant(
                UUID.randomUUID().toString(),
                new RequiredIdDocs(Arrays.asList(identityDocSet, selfieDocSet))
        );

        //make request

        long ts = Instant.now().getEpochSecond();
        String url = "/resources/applicants";
        String jsonBody = objectMapper.writeValueAsString(applicant);

        HttpURLConnection httpClient = (HttpURLConnection) new URL(SUMSUB_TEST_BASE_URL + url).openConnection();
        httpClient.setRequestMethod("POST");
        httpClient.setRequestProperty("Content-Type", "application/json");
        httpClient.setRequestProperty("X-App-Token", SUMSUB_APP_TOKEN);
        httpClient.setRequestProperty("X-App-Access-Sig", createSignature(ts, HttpMethod.POST, url, jsonBody.getBytes()));
        httpClient.setRequestProperty("X-App-Access-Ts", String.valueOf(ts));

        httpClient.setDoOutput(true);
        try (OutputStream os = httpClient.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = httpClient.getResponseCode();
        System.out.println("Sending 'POST' request to URL: " + SUMSUB_TEST_BASE_URL + url);
        System.out.println("responseCode: " + responseCode);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {

            String line;
            StringBuilder response = new StringBuilder();

            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            return objectMapper.readValue(response.toString(), Applicant.class).getId();
        }
    }


    public static void addDocument(String applicantId, String path) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        // https://developers.sumsub.com/api-reference/#adding-an-id-document

        long ts = Instant.now().getEpochSecond();
        String url = "/resources/applicants/" + applicantId + "/info/idDoc";

        System.out.println(SUMSUB_TEST_BASE_URL + url);

        String boundary = String.valueOf(System.currentTimeMillis());
        String body = MultipartUtil.getBody(boundary, "metadata", "{\"idDocType\":\"PASSPORT\",\"country\":\"ALB\"}", "content", path);

        HttpURLConnection httpClient = (HttpURLConnection) new URL("https://ykfawejygs.requestcatcher.com/test").openConnection();
        httpClient.setRequestMethod("POST");
        httpClient.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        httpClient.setRequestProperty("X-App-Token", SUMSUB_APP_TOKEN);
        httpClient.setRequestProperty("X-App-Access-Sig", createSignature(ts, HttpMethod.POST, url, body.getBytes()));
        httpClient.setRequestProperty("X-App-Access-Ts", String.valueOf(ts));

        httpClient.setDoOutput(true);
        try (OutputStream os = httpClient.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = httpClient.getResponseCode();
        System.out.println("Sending 'POST' request to URL: " + SUMSUB_TEST_BASE_URL + url);
        System.out.println("responseCode: " + responseCode);
    }

    public static String createSignature(long ts, HttpMethod httpMethod, String path, byte[] body) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(new SecretKeySpec(SUMSUB_SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        hmacSha256.update((ts + httpMethod.name() + path).getBytes(StandardCharsets.UTF_8));
        byte[] bytes = body == null ? hmacSha256.doFinal() : hmacSha256.doFinal(body);
        return Hex.encodeHexString(bytes);
    }
}



