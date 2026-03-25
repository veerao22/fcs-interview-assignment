package com.fulfilment.application.monolith.warehouses.domain.excpetions;

import com.fulfilment.application.monolith.exceptions.BusinessException;

public class InsufficientCapacityException extends BusinessException {

  public static final String ERROR_CODE = "INSUFFICIENT_CAPACITY";

  public InsufficientCapacityException(int newCapacity, int requiredStock) {
    super(
        "New warehouse capacity "
            + newCapacity
            + " cannot accommodate the required stock of "
            + requiredStock,
        ERROR_CODE,
        400);
  }
}
