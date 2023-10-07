class RequestInfo {
  String requestType;
  int statusCode;
  long startTime;
  long latency;

  public RequestInfo(long startTime, String requestType, long latency, int statusCode ){
    this.startTime = startTime;
    this.requestType = requestType;
    this.latency = latency;
    this.statusCode = statusCode;
  }

  public String getRequestType() {
    return requestType;
  }

  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getLatency() {
    return latency;
  }

  public void setLatency(long latency) {
    this.latency = latency;
  }

}