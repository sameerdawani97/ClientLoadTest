import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;


import java.io.File;
import java.io.IOException;

public class clientPost {

  public static void main(String[] args) throws IOException, InterruptedException {
    String ipAddress = "http://ec2-user@ec2-54-201-74-31.us-west-2.compute.amazonaws.com:8080/assignment1_war/albums";
    File imageFile = new File("src/main/resources/testImage.png");
    byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
    String boundary = "Boundary-" + System.currentTimeMillis();

    String CRLF = "\r\n";
    String requestBody = "--" + boundary + CRLF +
        "Content-Disposition: form-data; name=\"profile\"" + CRLF +
        "Content-Type: application/json" + CRLF +
        CRLF +
        "{\"artist\":\"Sex Pistols\",\"title\":\"Never Mind The Bollocks!\",\"year\":\"1977\"}" + CRLF +
        "--" + boundary + CRLF +
        "Content-Disposition: form-data; name=\"Image\"; filename=\"image.jpg\"" + CRLF +
        "Content-Type: application/octet-stream" + CRLF +
        CRLF;

    requestBody += new String(imageBytes, StandardCharsets.UTF_8) + CRLF;
    requestBody += "--" + boundary + "--" + CRLF;

    HttpClient httpClient = HttpClient.newHttpClient();
    HttpRequest postRequest = HttpRequest.newBuilder()
        .uri(URI.create(ipAddress))
        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
        .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
        .build();

    //CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(postRequest, HttpResponse.BodyHandlers.ofString());

    // Handle the response when it becomes available
//    responseFuture.thenAccept(response -> {
//      // Get and print the HTTP status code
//      int statusCode = response.statusCode();
//      System.out.println("Status Code: " + statusCode);
//
//      // Get and print the response body
//      String responseBody = response.body();
//      System.out.println("Response Body:\n" + responseBody);
//    });
    HttpResponse<String> response = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

    int statusCode = response.statusCode();
    System.out.println("Status Code: " + statusCode);

    // Get and print the response body
    String responseBody = response.body();
    System.out.println("Response Body:\n" + responseBody);

  }
}
