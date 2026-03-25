package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.excpetions.*;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.validators.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class.getName());

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  private final WarehouseValidator warehouseValidator;

  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore,
      LocationResolver locationResolver,
      WarehouseValidator warehouseValidator) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
    this.warehouseValidator = warehouseValidator;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    LOGGER.infov(
        "Replacing warehouse: buCode={0}, new location={1}, capacity={2}, stock={3}",
        newWarehouse.businessUnitCode,
        newWarehouse.location,
        newWarehouse.capacity,
        newWarehouse.stock);

    warehouseValidator.validateRequiredFields(newWarehouse);

    // Find current active warehouse by business unit code (must exist to replace)
    Warehouse current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null) {
      throw new WarehouseNotFoundException(newWarehouse.businessUnitCode);
    }

    // Capacity Accommodation: new warehouse's capacity must accommodate old warehouse's stock
    if (newWarehouse.capacity < current.stock) {
      throw new InsufficientCapacityException(newWarehouse.capacity, current.stock);
    }

    // Stock Matching: new warehouse's stock must match the previous warehouse's stock
    if (!newWarehouse.stock.equals(current.stock)) {
      throw new StockMismatchException(newWarehouse.stock, current.stock);
    }

    // Location Validation: new warehouse location must be valid
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);

    // Warehouse Creation Feasibility at new location
    long activeCountAtNewLocation = warehouseStore.countActiveByLocation(newWarehouse.location);
    boolean sameLocation = newWarehouse.location.equals(current.location);
    if (sameLocation) {
      // Replacing in place: count stays same (we archive one, create one)
      if (activeCountAtNewLocation > location.maxNumberOfWarehouses) {
        throw new MaxWarehousesReachedException(newWarehouse.location);
      }
    } else {
      // Different location: we need room for one more
      if (activeCountAtNewLocation >= location.maxNumberOfWarehouses) {
        throw new MaxWarehousesReachedException(newWarehouse.location);
      }
    }

    // Capacity and Stock Validation: total capacity at location and stock <= capacity
    int totalCapacityAtNewLocation = warehouseStore.totalCapacityByLocation(newWarehouse.location);
    if (sameLocation) {
      int capacityAfterReplace =
          totalCapacityAtNewLocation - current.capacity + newWarehouse.capacity;
      if (capacityAfterReplace > location.maxCapacity) {
        throw new CapacityExceededException(capacityAfterReplace, location.maxCapacity);
      }
    } else {
      if (totalCapacityAtNewLocation + newWarehouse.capacity > location.maxCapacity) {
        throw new CapacityExceededException(
            totalCapacityAtNewLocation + newWarehouse.capacity, location.maxCapacity);
      }
    }
    if (newWarehouse.stock > newWarehouse.capacity) {
      throw new CapacityExceededException(newWarehouse.stock, newWarehouse.capacity);
    }

    // Archive current warehouse
    current.archivedAt = LocalDateTime.now();
    warehouseStore.update(current);

    // Create new warehouse with same business unit code
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);

    LOGGER.infov("Warehouse replaced successfully: {0}", newWarehouse.businessUnitCode);
  }
}
