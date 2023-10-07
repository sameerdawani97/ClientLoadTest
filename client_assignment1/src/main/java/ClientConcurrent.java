import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClientConcurrent {
  private static final int INITIAL_THREADS_SIZE = 10;
  private static final int NUM_REQUESTS_PER_THREAD_FOR_INITIAL = 100;
  private static final int NUM_REQUESTS_PER_THREAD_FOR_GROUP = 1000;
  public static HttpClient httpClient = HttpClient.newHttpClient();


  public static void main(String[] args) throws IOException {
    if (args.length != 4) {
      System.err.println("Four arguments should be passed: ");
      System.err.println("1) Size of threads in group 2) number of thread groups 3) delay time in second 4) Server address");
      System.exit(1);
    }


    int threadGroupSize = Integer.parseInt(args[0]);
    int numOfGroups = Integer.parseInt(args[1]);
    int delayInSeconds = Integer.parseInt(args[2]);
    String ipAddress = args[3];

    //final int serverOption = Integer.parseInt(args[4]);
    final int albumId = 1;

    HttpRequest requestGet;

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
        "Content-Disposition: form-data; name=\"Image\"; filename=\"testImage.png\"" + CRLF +
        "Content-Type: application/octet-stream" + CRLF +
        CRLF;

    requestBody += new String(imageBytes, StandardCharsets.UTF_8) + CRLF;
    requestBody += "--" + boundary + "--" + CRLF;

    HttpRequest requestPost = HttpRequest.newBuilder()
        .uri(URI.create(ipAddress))
        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
        .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
        .build();


    requestGet = HttpRequest.newBuilder()
        .uri(URI.create(ipAddress + "/" + albumId))
        .GET()
        .build();



    // Create an HttpClient
    long startTime, endTime;
    System.out.println("Threads are running...");
    Thread[] initialThreads = new Thread[INITIAL_THREADS_SIZE];
    List<Thread> arrayList = new ArrayList<>();
    List<Thread> groupThreads = Collections.synchronizedList(arrayList);

    for (int i = 0; i < INITIAL_THREADS_SIZE; i++) {
      initialThreads[i] = new Thread(() -> callRequests(ipAddress, NUM_REQUESTS_PER_THREAD_FOR_INITIAL, requestGet, requestPost));
      initialThreads[i].start();
    }

    for (Thread thread : initialThreads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    startTime = System.currentTimeMillis();

    for (int i = 0; i < numOfGroups; i++) {
      System.out.println(i);
      // Create and start threadGroupSize threads
      //ExecutorService executor = Executors.newFixedThreadPool(threadGroupSize);

      for (int j = 0; j < threadGroupSize; j++) {
        Thread thread = new Thread(() -> callRequests(ipAddress, NUM_REQUESTS_PER_THREAD_FOR_GROUP, requestGet, requestPost));
        thread.start();
        groupThreads.add(thread);
      }


      try {
        Thread.sleep(delayInSeconds * 1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    for (Thread thread : groupThreads){
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }



    endTime = System.currentTimeMillis();
    long wallTime = (endTime - startTime)/1000;
    long throughput = 2*numOfGroups*threadGroupSize*NUM_REQUESTS_PER_THREAD_FOR_GROUP / wallTime;
    System.out.println("Wall Time: " + wallTime + " seconds");
    System.out.println("Throughput: " + throughput + "/sec");
  }

  private static void callRequests(String ipAddress, int numRequests, HttpRequest requestGet, HttpRequest requestPost) {
    //HttpClient httpClient = HttpClient.newHttpClient();
    for (int i = 0; i < numRequests; i++) {
      try {
        // Send a POST request
        sendPostRequest(ipAddress, requestPost);

        // Send a GET request
        sendGetRequest(ipAddress, requestGet);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static void sendPostRequest(String ipAddr, HttpRequest requestPost)
      throws IOException, InterruptedException {

    int retryCount = 0;
    int statusCode = 0;
    HttpResponse<String> response = null;
    boolean failed = true;
    while (retryCount < 5 && failed){

      response = httpClient.send(requestPost, HttpResponse.BodyHandlers.ofString());

      statusCode = response.statusCode();

      if (statusCode == 200 || statusCode ==201){
        failed = false;
      }
      else {
        retryCount+=1;
      }
    }


    //System.out.println("Status Code: " + statusCode);

    // Get and print the response body
    String responseBody = response.body();
    //System.out.println("Response Body:\n" + responseBody);

  }

  private static void sendGetRequest(String ipAddr, HttpRequest request) throws IOException {

    try {

      int statusCode = 0;
      HttpResponse<String> response = null;
      boolean failed = true;
      int retryCount = 0;
      while (retryCount < 5 && failed){

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        statusCode = response.statusCode();

        if (statusCode == 200 || statusCode ==201){
          failed = false;
        }
        else {
          retryCount+=1;
        }
      }

      //System.out.println("Status Code: " + statusCode);

      // Get and print the response body
      String responseBody = response.body();
      //System.out.println("Response Body:\n" + responseBody);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
