package DC_square.spring.apiPayload.exception;

import DC_square.spring.apiPayload.code.status.ErrorStatus;

public class TokenException extends RuntimeException {

  private final ErrorStatus errorStatus;

  public TokenException(ErrorStatus errorStatus) {
    super(errorStatus.getMessage());
    this.errorStatus = errorStatus;
  }

  public ErrorStatus getErrorStatus() {
    return errorStatus;
  }
}
