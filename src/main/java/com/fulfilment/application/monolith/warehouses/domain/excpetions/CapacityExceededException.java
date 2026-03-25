package com.fulfilment.application.monolith.warehouses.domain.excpetions;

import com.fulfilment.application.monolith.exceptions.BusinessException;

public class CapacityExceededException extends BusinessException {

  public static final String ERROR_CODE = "CAPACITY_EXCEEDED";

  public CapacityExceededException(int capacity, int maxCapacity) {
    super(
        "Warehouse capacity "
            + capacity
            + " exceeds maximum allowed capacity "
            + maxCapacity
            + " for this location",
        ERROR_CODE,
        400);
  }
}
