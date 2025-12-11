package DC_square.spring.web.dto.response;

public class ApiResponse {

  private boolean success;
  private int status;
  private String message;

  public ApiResponse(boolean success, int status, String message) {
    this.success = success;
    this.status = status;
    this.message = message;
  }
}

