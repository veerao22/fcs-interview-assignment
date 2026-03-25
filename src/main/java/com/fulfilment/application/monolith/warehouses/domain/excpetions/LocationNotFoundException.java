package com.fulfilment.application.monolith.warehouses.domain.excpetions;

import com.fulfilment.application.monolith.exceptions.BusinessException;

public class LocationNotFoundException extends BusinessException {

  public static final String ERROR_CODE = "LOCATION_NOT_FOUND";

  public LocationNotFoundException(String identifier) {
    super("Location not found: " + identifier, ERROR_CODE, 400);
  }
}
