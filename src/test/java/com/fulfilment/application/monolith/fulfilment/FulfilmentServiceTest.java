package com.fulfilment.application.monolith.fulfilment;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfilmentServiceTest {

  @Inject FulfilmentService fulfilmentService;
  @Inject FulfilmentTestDataHelper testData;

  @Nested
  @DisplayName("Assign and list")
  class AssignAndList {

    @Test
    void assign_whenAnyIdNull_throws() {
      var ex =
          assertThrows(
              FulfilmentConstraintException.class, () -> fulfilmentService.assign(null, 1L, 1L));
      assertTrue(ex.getMessage().contains("required"));
      assertThrows(
          FulfilmentConstraintException.class, () -> fulfilmentService.assign(1L, null, 1L));
      assertThrows(
          FulfilmentConstraintException.class, () -> fulfilmentService.assign(1L, 1L, null));
    }

    @Test
    void assign_whenStoreNotFound_throws() {
      var ex =
          assertThrows(
              FulfilmentConstraintException.class, () -> fulfilmentService.assign(999L, 1L, 1L));
      assertTrue(ex.getMessage().contains("Store not found"));
    }

    @Test
    void assign_whenProductNotFound_throws() {
      var ex =
          assertThrows(
              FulfilmentConstraintException.class, () -> fulfilmentService.assign(1L, 999L, 1L));
      assertTrue(ex.getMessage().contains("Product not found"));
    }

    @Test
    void assign_whenWarehouseNotFound_throws() {
      var ex =
          assertThrows(
              FulfilmentConstraintException.class, () -> fulfilmentService.assign(1L, 1L, 999L));
      assertTrue(ex.getMessage().contains("Warehouse not found"));
    }

    @Test
    void assign_whenWarehouseArchived_throws() {
      Long archivedWarehouseId = testData.createArchivedWarehouse();
      // Repository getById returns only active warehouses; archived are treated as not found
      var ex =
          assertThrows(
              FulfilmentConstraintException.class,
              () -> fulfilmentService.assign(1L, 1L, archivedWarehouseId));
      assertTrue(ex.getMessage().contains("Warehouse not found"), "Got: " + ex.getMessage());
    }

    @Test
    void assign_then_listByStore_returnsAssignment() {
      fulfilmentService.assign(2L, 2L, 2L);
      var list = fulfilmentService.listByStore(2L);
      assertTrue(
          list.stream().anyMatch(f -> f.storeId == 2 && f.productId == 2 && f.warehouseId == 2));
    }

    @Test
    void assign_idempotent_secondAssign_doesNotThrow() {
      fulfilmentService.assign(2L, 3L, 2L);
      fulfilmentService.assign(2L, 3L, 2L);
      assertEquals(
          1,
          fulfilmentService.listByStore(2L).stream()
              .filter(f -> f.productId == 3 && f.warehouseId == 2)
              .count());
    }

    @Test
    void unassign_removesAssignment() {
      fulfilmentService.assign(3L, 1L, 2L);
      fulfilmentService.unassign(3L, 1L, 2L);
      assertTrue(
          fulfilmentService.listByStore(3L).stream()
              .noneMatch(f -> f.productId == 1 && f.warehouseId == 2));
    }
  }

  @Nested
  @DisplayName("Constraint: max 2 warehouses per product per store")
  class MaxWarehousesPerProductPerStore {

    @Test
    void thirdWarehouseForSameProductAtStore_throws() {
      fulfilmentService.assign(1L, 1L, 1L);
      fulfilmentService.assign(1L, 1L, 2L);
      var ex =
          assertThrows(
              FulfilmentConstraintException.class, () -> fulfilmentService.assign(1L, 1L, 3L));
      assertTrue(ex.getMessage().contains("at most 2"));
    }
  }

  @Nested
  @DisplayName("Constraint: max 3 warehouses per store")
  class MaxWarehousesPerStore {

    @Test
    void fourthDistinctWarehouseForStore_throws() {
      Long warehouse4Id = testData.createWarehouse4();

      fulfilmentService.assign(1L, 1L, 1L);
      fulfilmentService.assign(1L, 2L, 2L);
      fulfilmentService.assign(1L, 3L, 3L);
      var ex =
          assertThrows(
              FulfilmentConstraintException.class,
              () -> fulfilmentService.assign(1L, 1L, warehouse4Id));
      assertTrue(
          ex.getMessage() != null && ex.getMessage().contains("Store can be fulfilled"),
          "Expected message about store warehouse limit, got: " + ex.getMessage());
    }
  }

  @Nested
  @DisplayName("Constraint: max 5 product types per warehouse")
  class MaxProductTypesPerWarehouse {

    @Test
    void sixthProductTypeForWarehouse_throws() {
      long[] productIds = testData.createProducts4_5_6();
      long id4 = productIds[0], id5 = productIds[1], id6 = productIds[2];

      fulfilmentService.assign(1L, 1L, 1L);
      fulfilmentService.assign(2L, 2L, 1L);
      fulfilmentService.assign(3L, 3L, 1L);
      fulfilmentService.assign(1L, id4, 1L);
      fulfilmentService.assign(2L, id5, 1L);
      var ex =
          assertThrows(
              FulfilmentConstraintException.class, () -> fulfilmentService.assign(3L, id6, 1L));
      assertTrue(ex.getMessage().contains("at most 5"));
    }
  }
}
