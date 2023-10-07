import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpResponse;

public class client {
  public static void main(String[] args) {
    // Create an HttpClient
    HttpClient httpClient = HttpClients.createDefault();

    // Define the API URL
    String baseUrl = "http://localhost:8080/assignment1_war_exploded/albums"; // Replace with your API endpoint

    int albumId = 1;
    String apiUrl = baseUrl + "/" + albumId;
    // Create an HTTP GET request
    HttpGet httpGet = new HttpGet(apiUrl);

    try {
      // Execute the request and get the response
      HttpResponse response = httpClient.execute(httpGet);

      // Check the response status code
      int statusCode = response.getStatusLine().getStatusCode();
      System.out.println("Response Status Code: " + statusCode);

      // Read the response content
      String responseBody = EntityUtils.toString(response.getEntity());
      System.out.println("Response Body: " + responseBody);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
