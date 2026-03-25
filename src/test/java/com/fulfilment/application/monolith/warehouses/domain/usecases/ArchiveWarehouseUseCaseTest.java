package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@QuarkusTest
public class ArchiveWarehouseUseCaseTest {

  @InjectMock WarehouseStore warehouseStore;

  @Inject ArchiveWarehouseUseCase useCase;

  private static Warehouse warehouse(String buCode, String location, int capacity, int stock) {
    var w = new Warehouse();
    w.businessUnitCode = buCode;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    return w;
  }

  @Test
  void archive_whenWarehouseHasNoBusinessUnitCode_throws() {
    var w = warehouse("  ", "ZWOLLE-001", 100, 10);

    assertThrows(WarehouseNotFoundException.class, () -> useCase.archive(w));
    verify(warehouseStore, never()).update(any());
  }

  @Test
  void archive_whenActive_setsArchivedAtAndUpdates() {
    var w = warehouse("MWH.001", "ZWOLLE-001", 100, 10);
    assertNull(w.archivedAt);

    useCase.archive(w);

    verify(warehouseStore).update(w);
    assertNotNull(w.archivedAt);
  }

  @Test
  void archive_whenAlreadyArchived_doesNotUpdate() {
    var w = warehouse("MWH.001", "ZWOLLE-001", 100, 10);
    w.archivedAt = LocalDateTime.now().minusDays(1);

    useCase.archive(w);

    verify(warehouseStore, never()).update(any());
  }
}
