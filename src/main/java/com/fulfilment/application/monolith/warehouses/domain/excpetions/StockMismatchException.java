package com.fulfilment.application.monolith.warehouses.domain.excpetions;

import com.fulfilment.application.monolith.exceptions.BusinessException;

public class StockMismatchException extends BusinessException {

  public static final String ERROR_CODE = "STOCK_MISMATCH";

  public StockMismatchException(int newStock, int expectedStock) {
    super(
        "New warehouse stock "
            + newStock
            + " does not match the expected stock of "
            + expectedStock,
        ERROR_CODE,
        400);
  }
}
