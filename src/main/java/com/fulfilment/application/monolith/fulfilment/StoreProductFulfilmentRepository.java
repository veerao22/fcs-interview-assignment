package com.fulfilment.application.monolith.fulfilment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class StoreProductFulfilmentRepository implements PanacheRepository<StoreProductFulfilment> {

  public long countWarehousesByStoreAndProduct(Long storeId, Long productId) {
    return count("storeId = ?1 and productId = ?2", storeId, productId);
  }

  public long countDistinctWarehousesByStore(Long storeId) {
    return getEntityManager()
        .createQuery(
            "select count(distinct f.warehouseId) from StoreProductFulfilment f where f.storeId = ?1",
            Long.class)
        .setParameter(1, storeId)
        .getSingleResult();
  }

  public long countDistinctProductsByWarehouse(Long warehouseId) {
    return getEntityManager()
        .createQuery(
            "select count(distinct f.productId) from StoreProductFulfilment f where f.warehouseId = ?1",
            Long.class)
        .setParameter(1, warehouseId)
        .getSingleResult();
  }

  public StoreProductFulfilment findByStoreAndProductAndWarehouse(
      Long storeId, Long productId, Long warehouseId) {
    return find(
            "storeId = ?1 and productId = ?2 and warehouseId = ?3", storeId, productId, warehouseId)
        .firstResult();
  }

  public List<StoreProductFulfilment> listByStore(Long storeId) {
    return list("storeId", storeId);
  }

  public List<StoreProductFulfilment> listByWarehouse(Long warehouseId) {
    return list("warehouseId", warehouseId);
  }

  /** Returns whether this warehouse is already used for this store (any product). */
  public boolean isWarehouseUsedForStore(Long storeId, Long warehouseId) {
    return count("storeId = ?1 and warehouseId = ?2", storeId, warehouseId) > 0;
  }
}
