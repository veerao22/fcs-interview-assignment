package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.excpetions.CapacityExceededException;
import com.fulfilment.application.monolith.warehouses.domain.excpetions.DuplicateBusinessUnitCodeException;
import com.fulfilment.application.monolith.warehouses.domain.excpetions.MaxWarehousesReachedException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.validators.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class.getName());

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  private final WarehouseValidator warehouseValidator;

  public CreateWarehouseUseCase(
          WarehouseStore warehouseStore,
          LocationResolver locationResolver,
          WarehouseValidator warehouseValidator) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
    this.warehouseValidator = warehouseValidator;
  }

  @Override
  public void create(Warehouse warehouse) {
    LOGGER.infov(
            "Creating warehouse: buCode={0}, location={1}, capacity={2}, stock={3}",
            warehouse.businessUnitCode, warehouse.location, warehouse.capacity, warehouse.stock);

    warehouseValidator.validateRequiredFields(warehouse);

    // Business Unit Code Verification: must not already exist (assignment constraint)
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null) {
      throw new DuplicateBusinessUnitCodeException(warehouse.businessUnitCode);
    }

    // Location Validation: must be an existing valid location (assignment constraint)
    Location location = locationResolver.resolveByIdentifier(warehouse.location);

    // Warehouse Creation Feasibility: max number of warehouses at location not reached (assignment
    // constraint)
    long activeCount = warehouseStore.countActiveByLocation(warehouse.location);
    if (activeCount >= location.maxNumberOfWarehouses) {
      throw new MaxWarehousesReachedException(warehouse.location);
    }

    // Capacity and Stock Validation: capacity must not exceed location max; warehouse must handle
    // stock (assignment constraint)
    int currentTotalCapacity = warehouseStore.totalCapacityByLocation(warehouse.location);
    if (currentTotalCapacity + warehouse.capacity > location.maxCapacity) {
      throw new CapacityExceededException(
              currentTotalCapacity + warehouse.capacity, location.maxCapacity);
    }
    if (warehouse.stock > warehouse.capacity) {
      throw new CapacityExceededException(warehouse.stock, warehouse.capacity);
    }

    // Set timestamps
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    // If all validations pass, create the warehouse
    warehouseStore.create(warehouse);
    LOGGER.infov("Warehouse created successfully: {0}", warehouse.businessUnitCode);
  }
}
