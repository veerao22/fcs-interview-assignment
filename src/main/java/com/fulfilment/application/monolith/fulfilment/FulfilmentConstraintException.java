package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.exceptions.BusinessException;

public class FulfilmentConstraintException extends BusinessException {

  public static final String ERROR_CODE = "FULFILMENT_CONSTRAINT";

  public FulfilmentConstraintException(String message) {
    super(message, ERROR_CODE, 400);
  }
}
