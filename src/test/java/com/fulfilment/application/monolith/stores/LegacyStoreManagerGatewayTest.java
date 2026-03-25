package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class LegacyStoreManagerGatewayTest {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  private static Store store(String name, int quantityProductsInStock) {
    Store s = new Store(name);
    s.id = 1L;
    s.quantityProductsInStock = quantityProductsInStock;
    return s;
  }

  @Test
  void createStoreOnLegacySystem_withValidStore_doesNotThrow() {
    Store store = store("LEGACY_TEST_STORE", 42);
    assertDoesNotThrow(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
  }

  @Test
  void updateStoreOnLegacySystem_withValidStore_doesNotThrow() {
    Store store = store("LEGACY_UPDATE_STORE", 10);
    assertDoesNotThrow(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store));
  }

  @Test
  void createStoreOnLegacySystem_withStoreWithNullName_doesNotThrow() {
    Store store = store("x", 0);
    store.name = null;
    assertDoesNotThrow(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
  }

  @Test
  void updateStoreOnLegacySystem_withStoreWithNullName_doesNotThrow() {
    Store store = store("x", 0);
    store.name = null;
    assertDoesNotThrow(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store));
  }

  @Test
  void createStoreOnLegacySystem_withEmptyName_doesNotThrow() {
    Store store = store("", 5);
    assertDoesNotThrow(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
  }
}
