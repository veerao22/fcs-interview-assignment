package com.fulfilment.application.monolith.warehouses.validators;

import com.fulfilment.application.monolith.warehouses.domain.excpetions.InvalidWarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WarehouseValidator {

  /**
   * Validates required fields and basic capacity/stock rules for a warehouse.
   *
   * @param warehouse the warehouse to validate
   * @throws InvalidWarehouseException if any validation check fails
   */
  public void validateRequiredFields(Warehouse warehouse) {
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new InvalidWarehouseException("Business unit code is required");
    }
    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new InvalidWarehouseException("Location is required");
    }
    if (warehouse.capacity == null) {
      throw new InvalidWarehouseException("Capacity is required");
    }
    if (warehouse.stock == null) {
      throw new InvalidWarehouseException("Stock is required");
    }
    if (warehouse.capacity <= 0) {
      throw new InvalidWarehouseException("Capacity must be positive");
    }
    if (warehouse.stock < 0) {
      throw new InvalidWarehouseException("Stock cannot be negative");
    }
  }
}
