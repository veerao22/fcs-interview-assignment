package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.*;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class CreateWarehouseUseCaseTest {

  @InjectMock WarehouseStore warehouseStore;

  @InjectMock LocationResolver locationResolver;

  @Inject CreateWarehouseUseCase useCase;

  private Warehouse buildWarehouse(String buCode, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = buCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }

  @Test
  public void testCreateWarehouseSuccessfully() {
    Warehouse warehouse = buildWarehouse("MWH.NEW", "AMSTERDAM-001", 30, 5);

    when(warehouseStore.findActiveByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001")).thenReturn(1L);
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001")).thenReturn(50);

    useCase.create(warehouse);

    verify(warehouseStore).create(warehouse);
    assertNotNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }

  @Test
  public void testCreateWarehouseThrowsDuplicateBusinessUnitCode() {
    Warehouse warehouse = buildWarehouse("MWH.001", "AMSTERDAM-001", 30, 5);
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 100, 10);

    when(warehouseStore.findActiveByBusinessUnitCode("MWH.001")).thenReturn(existing);

    assertThrows(DuplicateBusinessUnitCodeException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }

  @Test
  public void testCreateWarehouseThrowsLocationNotFound() {
    Warehouse warehouse = buildWarehouse("MWH.NEW", "INVALID-LOC", 30, 5);

    when(warehouseStore.findActiveByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("INVALID-LOC"))
        .thenThrow(new LocationNotFoundException("INVALID-LOC"));

    assertThrows(LocationNotFoundException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }

  @Test
  public void testCreateWarehouseThrowsMaxWarehousesReached() {
    Warehouse warehouse = buildWarehouse("MWH.NEW", "ZWOLLE-001", 30, 5);

    when(warehouseStore.findActiveByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    // ZWOLLE-001: maxNumberOfWarehouses=1
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.countActiveByLocation("ZWOLLE-001")).thenReturn(1L);

    assertThrows(MaxWarehousesReachedException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }

  @Test
  public void testCreateWarehouseThrowsCapacityExceededForLocation() {
    Warehouse warehouse = buildWarehouse("MWH.NEW", "AMSTERDAM-001", 60, 5);

    when(warehouseStore.findActiveByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    // AMSTERDAM-001: maxCapacity=100
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001")).thenReturn(1L);
    // Already 50 capacity used + 60 new = 110 > 100
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001")).thenReturn(50);

    assertThrows(CapacityExceededException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }

  @Test
  public void testCreateWarehouseThrowsCapacityExceededStockOverCapacity() {
    Warehouse warehouse = buildWarehouse("MWH.NEW", "AMSTERDAM-001", 30, 50);

    when(warehouseStore.findActiveByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001")).thenReturn(0L);
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001")).thenReturn(0);

    // stock=50 > capacity=30
    assertThrows(CapacityExceededException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }

  // --- Boundary and extra coverage ---

  @Test
  public void testCreateWarehouseSuccessWhenCapacityExactlyAtMax() {
    // currentTotal 70 + new 30 = 100 == maxCapacity 100 -> allowed
    Warehouse warehouse = buildWarehouse("MWH.BOUNDARY", "AMSTERDAM-001", 30, 10);

    when(warehouseStore.findActiveByBusinessUnitCode("MWH.BOUNDARY")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001")).thenReturn(2L);
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001")).thenReturn(70);

    useCase.create(warehouse);

    verify(warehouseStore).create(warehouse);
    assertNotNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }

  @Test
  public void testCreateWarehouseSuccessWhenNoWarehousesAtLocationYet() {
    Warehouse warehouse = buildWarehouse("MWH.FIRST", "TILBURG-001", 40, 20);

    when(warehouseStore.findActiveByBusinessUnitCode("MWH.FIRST")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("TILBURG-001"))
        .thenReturn(new Location("TILBURG-001", 1, 40));
    when(warehouseStore.countActiveByLocation("TILBURG-001")).thenReturn(0L);
    when(warehouseStore.totalCapacityByLocation("TILBURG-001")).thenReturn(0);

    useCase.create(warehouse);

    verify(warehouseStore).create(warehouse);
    assertNotNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }

  @Test
  public void testCreateWarehouseThrowsWhenActiveCountEqualsMax() {
    // count=1, maxNumberOfWarehouses=1 -> no room
    Warehouse warehouse = buildWarehouse("MWH.NEW", "ZWOLLE-001", 10, 5);

    when(warehouseStore.findActiveByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.countActiveByLocation("ZWOLLE-001")).thenReturn(1L);

    assertThrows(MaxWarehousesReachedException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }

  @Test
  public void testCreateWarehouseThrowsWhenTotalCapacityWouldExceedByOne() {
    // 50 + 51 = 101 > 100
    Warehouse warehouse = buildWarehouse("MWH.OVER", "AMSTERDAM-001", 51, 10);

    when(warehouseStore.findActiveByBusinessUnitCode("MWH.OVER")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-001")).thenReturn(1L);
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-001")).thenReturn(50);

    assertThrows(CapacityExceededException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }

  @Test
  public void testCreateWarehouseSuccessWhenStockEqualsCapacity() {
    Warehouse warehouse = buildWarehouse("MWH.FULL", "AMSTERDAM-002", 50, 50);

    when(warehouseStore.findActiveByBusinessUnitCode("MWH.FULL")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-002"))
        .thenReturn(new Location("AMSTERDAM-002", 3, 75));
    when(warehouseStore.countActiveByLocation("AMSTERDAM-002")).thenReturn(0L);
    when(warehouseStore.totalCapacityByLocation("AMSTERDAM-002")).thenReturn(0);

    useCase.create(warehouse);

    verify(warehouseStore).create(warehouse);
    assertNotNull(warehouse.createdAt);
  }

  // --- InvalidWarehouseException: required fields and capacity/stock rules ---

  @Test
  public void testCreateWarehouseThrowsWhenCapacityIsNull() {
    Warehouse warehouse = buildWarehouse("MWH.NEW", "AMSTERDAM-001", 30, 5);
    warehouse.capacity = null;

    assertThrows(InvalidWarehouseException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }

  @Test
  public void testCreateWarehouseThrowsWhenStockIsNull() {
    Warehouse warehouse = buildWarehouse("MWH.NEW", "AMSTERDAM-001", 30, 5);
    warehouse.stock = null;

    assertThrows(InvalidWarehouseException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }

  @Test
  public void testCreateWarehouseThrowsWhenBusinessUnitCodeIsBlank() {
    Warehouse warehouse = buildWarehouse("  ", "AMSTERDAM-001", 30, 5);

    assertThrows(InvalidWarehouseException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }

  @Test
  public void testCreateWarehouseThrowsWhenLocationIsBlank() {
    Warehouse warehouse = buildWarehouse("MWH.NEW", "  ", 30, 5);

    assertThrows(InvalidWarehouseException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }

  @Test
  public void testCreateWarehouseThrowsWhenCapacityIsZero() {
    Warehouse warehouse = buildWarehouse("MWH.NEW", "AMSTERDAM-001", 0, 0);

    assertThrows(InvalidWarehouseException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }

  @Test
  public void testCreateWarehouseThrowsWhenStockIsNegative() {
    Warehouse warehouse = buildWarehouse("MWH.NEW", "AMSTERDAM-001", 30, -1);

    assertThrows(InvalidWarehouseException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(Mockito.any());
  }
}
