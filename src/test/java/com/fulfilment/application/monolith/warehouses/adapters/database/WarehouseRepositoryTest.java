package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class WarehouseRepositoryTest {

  @Inject
  WarehouseRepository warehouseRepository;

  @Test
  public void testGetAllReturnsSeedData() {
    List<Warehouse> warehouses = warehouseRepository.getAll();

    // import.sql seeds at least 3 warehouses (other tests may add more)
    assertTrue(warehouses.size() >= 3);
  }

  @Test
  public void testFindByBusinessUnitCodeReturnsExisting() {
    // MWH.001 exists in import.sql
    Warehouse warehouse = warehouseRepository.findByBusinessUnitCode("MWH.001");

    assertNotNull(warehouse);
    assertEquals("MWH.001", warehouse.businessUnitCode);
    assertEquals("ZWOLLE-001", warehouse.location);
    assertEquals(100, warehouse.capacity);
    assertEquals(10, warehouse.stock);
  }

  @Test
  public void testFindByBusinessUnitCodeReturnsNullForNonExistent() {
    Warehouse warehouse = warehouseRepository.findByBusinessUnitCode("NON_EXISTENT");

    assertNull(warehouse);
  }

  @Test
  @TestTransaction
  public void testCreateAndFindWarehouse() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.TEST.001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 60;
    warehouse.stock = 10;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    warehouseRepository.create(warehouse);

    Warehouse found = warehouseRepository.findByBusinessUnitCode("MWH.TEST.001");
    assertNotNull(found);
    assertEquals("MWH.TEST.001", found.businessUnitCode);
    assertEquals("AMSTERDAM-001", found.location);
    assertEquals(60, found.capacity);
    assertEquals(10, found.stock);
    assertNotNull(found.createdAt);
    assertNull(found.archivedAt);
  }

  @Test
  @TestTransaction
  public void testUpdateWarehouse() {
    // MWH.012 exists in import.sql with capacity=50, stock=5
    Warehouse warehouse = warehouseRepository.findByBusinessUnitCode("MWH.012");
    assertNotNull(warehouse);

    warehouse.capacity = 80;
    warehouse.stock = 15;
    warehouseRepository.update(warehouse);

    Warehouse updated = warehouseRepository.findByBusinessUnitCode("MWH.012");
    assertNotNull(updated);
    assertEquals(80, updated.capacity);
    assertEquals(15, updated.stock);
  }

  @Test
  @TestTransaction
  public void testRemoveWarehouse() {
    // Create one first, then remove it
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.TEST.REMOVE";
    warehouse.location = "EINDHOVEN-001";
    warehouse.capacity = 30;
    warehouse.stock = 5;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    warehouseRepository.create(warehouse);

    Warehouse found = warehouseRepository.findByBusinessUnitCode("MWH.TEST.REMOVE");
    assertNotNull(found);

    warehouseRepository.remove(found);

    Warehouse removed = warehouseRepository.findByBusinessUnitCode("MWH.TEST.REMOVE");
    assertNull(removed);
  }

  @Test
  @TestTransaction
  public void testGetAllExcludesArchivedWarehouses() {
    List<Warehouse> before = warehouseRepository.getAll();
    assertTrue(
            before.stream().anyMatch(w -> "MWH.001".equals(w.businessUnitCode)),
            "MWH.001 should be in getAll() when active");

    Warehouse wh = warehouseRepository.findByBusinessUnitCode("MWH.001");
    assertNotNull(wh);
    wh.archivedAt = LocalDateTime.now();
    warehouseRepository.update(wh);

    List<Warehouse> after = warehouseRepository.getAll();
    assertTrue(
            after.stream().noneMatch(w -> "MWH.001".equals(w.businessUnitCode)),
            "Archived MWH.001 must not appear in getAll()");
  }

  @Test
  @TestTransaction
  public void testGetByIdReturnsNullWhenWarehouseArchived() {
    // import.sql: id 1 = MWH.001
    assertNotNull(warehouseRepository.getById(1L));

    Warehouse wh = warehouseRepository.findByBusinessUnitCode("MWH.001");
    wh.archivedAt = LocalDateTime.now();
    warehouseRepository.update(wh);

    assertNull(warehouseRepository.getById(1L), "getById must return null for archived warehouse");
  }

  @Test
  @TestTransaction
  public void testFindByBusinessUnitCodeReturnsNullWhenArchived() {
    assertNotNull(warehouseRepository.findByBusinessUnitCode("MWH.001"));

    Warehouse wh = warehouseRepository.findByBusinessUnitCode("MWH.001");
    wh.archivedAt = LocalDateTime.now();
    warehouseRepository.update(wh);

    assertNull(
            warehouseRepository.findByBusinessUnitCode("MWH.001"),
            "findActiveByBusinessUnitCode must return null when warehouse is archived");
  }
}
