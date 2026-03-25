package com.fulfilment.application.monolith.warehouses.domain.excpetions;

import com.fulfilment.application.monolith.exceptions.BusinessException;

public class InvalidWarehouseException extends BusinessException {

  public static final String ERROR_CODE = "INVALID_WAREHOUSE";

  public InvalidWarehouseException(String message) {
    super(message, ERROR_CODE, 400);
  }
}
