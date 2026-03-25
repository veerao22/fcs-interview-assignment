package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.excpetions.*;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ReplaceWarehouseUseCaseTest {

  @InjectMock WarehouseStore warehouseStore;

  @InjectMock LocationResolver locationResolver;

  @Inject ReplaceWarehouseUseCase useCase;

  private static Warehouse warehouse(
      String buCode, String location, Integer capacity, Integer stock) {
    var w = new Warehouse();
    w.businessUnitCode = buCode;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    return w;
  }

  // --- validateRequiredFields branches ---
  @Test
  void replace_whenBusinessUnitCodeBlank_throwsInvalidWarehouseException() {
    Warehouse newWh = warehouse("  ", "AMSTERDAM-001", 100, 50);
    assertThrows(InvalidWarehouseException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_whenLocationBlank_throwsInvalidWarehouseException() {
    Warehouse newWh = warehouse("MWH.001", "  ", 100, 50);
    assertThrows(InvalidWarehouseException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_whenCapacityNull_throwsInvalidWarehouseException() {
    Warehouse newWh = warehouse("MWH.001", "AMSTERDAM-001", null, 50);
    assertThrows(InvalidWarehouseException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_whenStockNull_throwsInvalidWarehouseException() {
    Warehouse newWh = warehouse("MWH.001", "AMSTERDAM-001", 100, null);
    assertThrows(InvalidWarehouseException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_whenCapacityZero_throwsInvalidWarehouseException() {
    Warehouse newWh = warehouse("MWH.001", "AMSTERDAM-001", 0, 50);
    assertThrows(InvalidWarehouseException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_whenStockNegative_throwsInvalidWarehouseException() {
    Warehouse newWh = warehouse("MWH.001", "AMSTERDAM-001", 100, -1);
    assertThrows(InvalidWarehouseException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_whenCurrentNotFound_throwsWarehouseNotFoundException() {
    Warehouse newWh = warehouse("MWH.001", "AMSTERDAM-001", 100, 50);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null);

    assertThrows(WarehouseNotFoundException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_whenLocationNotFound_throwsLocationNotFoundException() {
    Warehouse current = warehouse("MWH.001", "ZWOLLE-001", 100, 50);
    Warehouse newWh = warehouse("MWH.001", "UNKNOWN-LOC", 100, 50);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
    when(locationResolver.resolveByIdentifier("UNKNOWN-LOC"))
        .thenThrow(new LocationNotFoundException("UNKNOWN-LOC"));

    assertThrows(LocationNotFoundException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_whenNewCapacityLessThanOldStock_throwsInsufficientCapacityException() {
    Warehouse current = warehouse("MWH.001", "ZWOLLE-001", 100, 80);
    Warehouse newWh = warehouse("MWH.001", "AMSTERDAM-001", 50, 80); // capacity 50 < stock 80
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);

    assertThrows(InsufficientCapacityException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_whenNewStockDoesNotMatchOldStock_throwsStockMismatchException() {
    Warehouse current = warehouse("MWH.001", "ZWOLLE-001", 100, 50);
    Warehouse newWh = warehouse("MWH.001", "AMSTERDAM-001", 100, 30); // stock 30 != 50
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001")).thenReturn(0L);
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001")).thenReturn(0);

    assertThrows(StockMismatchException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replace_whenValid_archivesCurrentAndCreatesNew() {
    Warehouse current = warehouse("MWH.001", "ZWOLLE-001", 100, 50);
    Warehouse newWh =
        warehouse("MWH.001", "AMSTERDAM-001", 80, 50); // same stock, capacity >= stock
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001")).thenReturn(1L);
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001")).thenReturn(20); // 20 + 80 <= 100

    useCase.replace(newWh);

    verify(warehouseStore).update(current);
    verify(warehouseStore).create(newWh);
    assert current.archivedAt != null;
    assert newWh.createdAt != null;
    assert newWh.archivedAt == null;
  }

  // --- Same location (replace in place) ---
  @Test
  void replace_whenValid_sameLocation_archivesCurrentAndCreatesNew() {
    Warehouse current = warehouse("MWH.001", "AMSTERDAM-001", 50, 30);
    Warehouse newWh = warehouse("MWH.001", "AMSTERDAM-001", 60, 30); // same location
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001"))
        .thenReturn(2L); // same loc: 2 active
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001"))
        .thenReturn(40); // 40 - 50 + 60 = 50 <= 100

    useCase.replace(newWh);

    verify(warehouseStore).update(current);
    verify(warehouseStore).create(newWh);
  }

  // --- MaxWarehousesReachedException: same location ---
  @Test
  void replace_whenSameLocationAndCountExceedsMax_throwsMaxWarehousesReachedException() {
    Warehouse current = warehouse("MWH.001", "AMSTERDAM-001", 50, 30);
    Warehouse newWh = warehouse("MWH.001", "AMSTERDAM-001", 60, 30);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 2, 100)); // max 2 warehouses
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001"))
        .thenReturn(2L); // already 2, same loc: 2 > 2 is false... need count > max. So 3 > 2.
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001")).thenReturn(50);
    // sameLocation true, activeCountAtNewLocation 3 > 2 → MaxWarehousesReached
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001")).thenReturn(3L);

    assertThrows(MaxWarehousesReachedException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  // --- MaxWarehousesReachedException: different location ---
  @Test
  void replace_whenDifferentLocationAndCountAtMax_throwsMaxWarehousesReachedException() {
    Warehouse current = warehouse("MWH.001", "ZWOLLE-001", 100, 50);
    Warehouse newWh = warehouse("MWH.001", "AMSTERDAM-001", 80, 50);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 2, 100));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001")).thenReturn(2L); // >= 2, no room
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001")).thenReturn(50);

    assertThrows(MaxWarehousesReachedException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  // --- CapacityExceededException: same location (capacityAfterReplace > maxCapacity) ---
  @Test
  void replace_whenSameLocationAndTotalCapacityExceedsMax_throwsCapacityExceededException() {
    Warehouse current = warehouse("MWH.001", "AMSTERDAM-001", 20, 10);
    Warehouse newWh =
        warehouse("MWH.001", "AMSTERDAM-001", 90, 10); // 20 - 20 + 90 = 90, but max 50
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 50));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001")).thenReturn(1L);
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001"))
        .thenReturn(20); // 20 - 20 + 90 = 90 > 50

    assertThrows(CapacityExceededException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  // --- CapacityExceededException: different location ---
  @Test
  void replace_whenDifferentLocationAndTotalCapacityExceedsMax_throwsCapacityExceededException() {
    Warehouse current = warehouse("MWH.001", "ZWOLLE-001", 100, 50);
    Warehouse newWh =
        warehouse("MWH.001", "AMSTERDAM-001", 60, 50); // existing cap 50, 50+60=110 > 100
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001")).thenReturn(1L);
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001")).thenReturn(50); // 50 + 60 > 100

    assertThrows(CapacityExceededException.class, () -> useCase.replace(newWh));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  // --- CapacityExceededException: stock > capacity (defensive check after stock match) ---
  // Note: With stock == current.stock and capacity >= current.stock, we have capacity >= stock,
  // so stock > capacity is unreachable in normal flow. This test uses capacity >= current.stock
  // but stock 51 != current.stock 50 to pass the capacity check, then we'd need to hit stock >
  // capacity
  // - but we throw StockMismatch first. So the stock>capacity branch is only reachable if
  // validation
  // order changes. We test a scenario that exercises the final capacity check: capacity equals
  // stock.
  @Test
  void replace_whenNewStockEqualsNewCapacity_passesStockCapacityCheck() {
    Warehouse current = warehouse("MWH.001", "ZWOLLE-001", 100, 50);
    Warehouse newWh = warehouse("MWH.001", "AMSTERDAM-001", 50, 50); // stock == capacity
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001")).thenReturn(0L);
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001")).thenReturn(0);

    useCase.replace(newWh);
    verify(warehouseStore).update(current);
    verify(warehouseStore).create(newWh);
  }
}
