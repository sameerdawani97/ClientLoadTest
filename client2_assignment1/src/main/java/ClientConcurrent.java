import static java.util.Collections.*;
import java.io.File;
import java.io.FileWriter;
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
    List<RequestInfo> csvRecord = new ArrayList<>();
    //List<RequestInfo> csvRecord = Collections.synchronizedList(arrayList1);


    for (int i = 0; i < INITIAL_THREADS_SIZE; i++) {
      initialThreads[i] = new Thread(() -> callRequests(ipAddress, NUM_REQUESTS_PER_THREAD_FOR_INITIAL, requestGet, requestPost, csvRecord));
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
      //System.out.println(i);
      // Create and start threadGroupSize threads
      //ExecutorService executor = Executors.newFixedThreadPool(threadGroupSize);

      for (int j = 0; j < threadGroupSize; j++) {
        Thread thread = new Thread(() -> callRequests(ipAddress, NUM_REQUESTS_PER_THREAD_FOR_GROUP, requestGet, requestPost, csvRecord));
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

    List<Long> values = new ArrayList<>();
    System.out.println("Mean Latency: " + calculateMean(csvRecord, values) + " milliseconds");
    // Sort the list in ascending order
    Collections.sort(values);
    System.out.println("Median Latency: " + calculateMedian(values) + " milliseconds");
    System.out.println("99th percentile Latency: " + calculate99thPercentile(values) + " milliseconds");
    System.out.println("Maximum Latency: " + max(values) + " milliseconds");
    System.out.println("Minimum Latency: " + min(values) + " milliseconds");
    writeCsv(csvRecord);

  }

  /**
   * This method is to calculate median value
   * @param values values
   * @return median value as double.
   */
  private static double calculateMedian(List<Long> values){
    if (values == null || values.isEmpty()) {
      throw new IllegalArgumentException("The list of values is empty or null.");
    }

    int size = values.size();
    int middle = size / 2;

    if (size % 2 == 1) {
      // Odd number of values, return the middle value
      return values.get(middle);
    } else {
      // Even number of values, return the average of the two middle values
      double middleValue1 = values.get(middle - 1);
      double middleValue2 = values.get(middle);
      return (middleValue1 + middleValue2) / 2.0;
    }
  }

  /**
   * This method is used to calculate mean value.
   * @param csvRecord csvRecord
   * @param values values
   * @return mean value
   */
  private static double calculateMean(List<RequestInfo> csvRecord, List<Long> values){
    double mean = 0.0;
    for (RequestInfo requestInfo : csvRecord){
      mean+=requestInfo.latency;
      values.add(requestInfo.latency);
    }
    mean = mean / csvRecord.size();
    return mean;
  }

  /**
   * This method is to calculate the 99th percentile.
   * @param values values
   * @return 99th percentile
   */
  private static double calculate99thPercentile(List<Long> values){
    if (values == null || values.isEmpty()) {
      throw new IllegalArgumentException("The list of values is empty or null.");
    }

    int size = values.size();
    double index = 0.99 * (size - 1);

    if (index == Math.floor(index)) {
      // If the index is an integer, return the value at that index
      return values.get((int) index);
    } else {
      // Interpolate between the two nearest values
      int lowerIndex = (int) Math.floor(index);
      int upperIndex = (int) Math.ceil(index);
      double lowerValue = values.get(lowerIndex);
      double upperValue = values.get(upperIndex);
      double fraction = index - lowerIndex;
      return lowerValue + fraction * (upperValue - lowerValue);
    }
  }

  /**
   * Method to write to csv file
   * @param csvRecord csvRecord
   */
  private static void writeCsv(List<RequestInfo> csvRecord){
    String filePath = "responseRecord.csv";

    try (FileWriter writer = new FileWriter(filePath)) {
      // Write header row
      writer.append("StartTime, RequestType, Latency, StatusCode");
      writer.append("\n");

      for (RequestInfo requestInfo : csvRecord){
        writer.append(requestInfo.startTime + ", " + requestInfo.requestType + ", "
            + requestInfo.latency + ", " + requestInfo.statusCode);
        writer.append("\n");
      }

      System.out.println("CSV file written successfully.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method is used to call 1000 request of each type.
   * @param ipAddress ipAddress
   * @param numRequests numRequests
   * @param requestGet requestGet
   * @param requestPost requestPost
   * @param csvRecord csvRecord
   */
  private static void callRequests(String ipAddress, int numRequests, HttpRequest requestGet, HttpRequest requestPost, List<RequestInfo> csvRecord) {
    //HttpClient httpClient = HttpClient.newHttpClient();
    for (int i = 0; i < numRequests; i++) {
      try {
        // Send a POST request
        sendPostRequest(ipAddress, requestPost, csvRecord);

        // Send a GET request
        sendGetRequest(ipAddress, requestGet, csvRecord);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * This method is used to send post request to api
   * @param ipAddr ipAddr
   * @param requestPost requestPost
   * @param csvRecord csvRecord
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   */
  private static void sendPostRequest(String ipAddr, HttpRequest requestPost, List<RequestInfo> csvRecord)
      throws IOException, InterruptedException {

    long startRequestTime, endRequestTime;
    int retryCount = 0;
    int statusCode = 0;
    HttpResponse<String> response = null;
    boolean failed = true;
    startRequestTime = System.currentTimeMillis();
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

    endRequestTime = System.currentTimeMillis();
    long requestLatency = endRequestTime - startRequestTime;
    RequestInfo requestInfo = new RequestInfo(startRequestTime, "POST", requestLatency, statusCode);
    csvRecord.add(requestInfo);

  }

  /**
   * This method is used to send get request to api.
   * @param ipAddr ipAddr
   * @param request request
   * @param csvRecord csvRecord
   * @throws IOException IOException
   */
  private static void sendGetRequest(String ipAddr, HttpRequest request, List<RequestInfo> csvRecord) throws IOException {

    try {

      long startRequestTime, endRequestTime;
      int statusCode = 0;
      HttpResponse<String> response = null;
      boolean failed = true;
      int retryCount = 0;

      startRequestTime = System.currentTimeMillis();

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

      endRequestTime = System.currentTimeMillis();
      long requestLatency = endRequestTime - startRequestTime;
      RequestInfo requestInfo = new RequestInfo(startRequestTime, "GET", requestLatency, statusCode);
      csvRecord.add(requestInfo);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
