package com.fulfilment.application.monolith.warehouses.domain.excpetions;

import com.fulfilment.application.monolith.exceptions.BusinessException;

public class DuplicateBusinessUnitCodeException extends BusinessException {

  public static final String ERROR_CODE = "DUPLICATE_BUSINESS_UNIT_CODE";

  public DuplicateBusinessUnitCodeException(String businessUnitCode) {
    super("Business unit code already exists: " + businessUnitCode, ERROR_CODE, 400);
  }
}
