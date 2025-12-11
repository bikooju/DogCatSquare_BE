package DC_square.spring.apiPayload.exception.handler;

import DC_square.spring.apiPayload.code.BaseErrorCode;
import DC_square.spring.apiPayload.exception.GeneralException;

public class TempHandler extends GeneralException {

  public TempHandler(BaseErrorCode errorCode) {
    super(errorCode);
  }
}