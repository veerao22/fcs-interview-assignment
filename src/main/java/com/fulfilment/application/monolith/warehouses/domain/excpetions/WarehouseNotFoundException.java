package com.fulfilment.application.monolith.warehouses.domain.excpetions;

import com.fulfilment.application.monolith.exceptions.BusinessException;

public class WarehouseNotFoundException extends BusinessException {

  public static final String ERROR_CODE = "WAREHOUSE_NOT_FOUND";

  public WarehouseNotFoundException(String businessUnitCode) {
    super("Warehouse not found with business unit code: " + businessUnitCode, ERROR_CODE, 404);
  }

  public WarehouseNotFoundException(Long id) {
    super("Warehouse not found with id: " + id, ERROR_CODE, 404);
  }
}
