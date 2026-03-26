package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

/** Helper to create test data inside a transaction (for use from @QuarkusTest). */
@ApplicationScoped
public class FulfilmentTestDataHelper {

  @Inject ProductRepository productRepository;
  @Inject WarehouseRepository warehouseRepository;

  @Transactional
  public Long createWarehouse4() {
    DbWarehouse w = new DbWarehouse();
    w.businessUnitCode = "MWH.034";
    w.location = "ROTTERDAM-001";
    w.capacity = 40;
    w.stock = 0;
    w.createdAt = LocalDateTime.of(2024, 1, 1, 0, 0);
    w.archivedAt = null;
    warehouseRepository.persist(w);
    return w.id;
  }

  /** Creates an archived warehouse for tests that assert "Cannot assign archived warehouse". */
  @Transactional
  public Long createArchivedWarehouse() {
    DbWarehouse w = new DbWarehouse();
    w.businessUnitCode = "MWH.ARCHIVED";
    w.location = "ZWOLLE-001";
    w.capacity = 20;
    w.stock = 0;
    w.createdAt = LocalDateTime.of(2023, 6, 1, 0, 0);
    w.archivedAt = LocalDateTime.of(2024, 6, 1, 0, 0);
    warehouseRepository.persist(w);
    return w.id;
  }

  @Transactional
  public long[] createProducts4_5_6() {
    Product p4 = new Product("PROD4");
    p4.stock = 1;
    productRepository.persist(p4);
    Product p5 = new Product("PROD5");
    p5.stock = 1;
    productRepository.persist(p5);
    Product p6 = new Product("PROD6");
    p6.stock = 1;
    productRepository.persist(p6);
    return new long[] {p4.id, p5.id, p6.id};
  }
}
