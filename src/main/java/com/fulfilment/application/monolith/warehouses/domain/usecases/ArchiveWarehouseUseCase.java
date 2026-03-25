package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.excpetions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ArchiveWarehouseUseCase.class.getName());

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    // Caller must pass a non-null warehouse with businessUnitCode (e.g. loaded by id)
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new WarehouseNotFoundException("unknown");
    }

    // Idempotent: already archived → no-op
    if (warehouse.archivedAt != null) {
      LOGGER.infov("Warehouse already archived: {0}", warehouse.businessUnitCode);
      return;
    }

    warehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(warehouse);
    LOGGER.infov("Warehouse archived successfully: {0}", warehouse.businessUnitCode);

  }
}
