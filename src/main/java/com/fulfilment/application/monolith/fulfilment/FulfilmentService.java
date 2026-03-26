package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class FulfilmentService {

  private static final Logger LOGGER = Logger.getLogger(FulfilmentService.class.getName());

  private static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  private static final int MAX_WAREHOUSES_PER_STORE = 3;
  private static final int MAX_PRODUCT_TYPES_PER_WAREHOUSE = 5;

  @Inject StoreProductFulfilmentRepository fulfilmentRepository;
  @Inject ProductRepository productRepository;
  @Inject WarehouseRepository warehouseRepository;

  /**
   * Assigns a warehouse to fulfil a product for a store. Enforces: (1) at most 2 warehouses per
   * product per store, (2) at most 3 warehouses per store, (3) at most 5 product types per
   * warehouse. Idempotent if the assignment already exists.
   *
   * @throws FulfilmentConstraintException if store/product/warehouse missing or constraints would
   *     be violated
   */
  @Transactional
  public void assign(Long storeId, Long productId, Long warehouseId) {
    if (storeId == null || productId == null || warehouseId == null) {
      throw new FulfilmentConstraintException(
          "Store, product and warehouse identifiers are required");
    }
    Store store = Store.findById(storeId);
    if (store == null) {
      throw new FulfilmentConstraintException("Store not found: " + storeId);
    }
    Product product = productRepository.findById(productId);
    if (product == null) {
      throw new FulfilmentConstraintException("Product not found: " + productId);
    }
    Warehouse warehouse = warehouseRepository.getById(warehouseId);
    if (warehouse == null) {
      throw new FulfilmentConstraintException("Warehouse not found: " + warehouseId);
    }
    if (warehouse.archivedAt != null) {
      throw new FulfilmentConstraintException("Cannot assign archived warehouse: " + warehouseId);
    }

    StoreProductFulfilment existing =
        fulfilmentRepository.findByStoreAndProductAndWarehouse(storeId, productId, warehouseId);
    if (existing != null) {
      return; // idempotent
    }

    long warehousesForProductAtStore =
        fulfilmentRepository.countWarehousesByStoreAndProduct(storeId, productId);
    if (warehousesForProductAtStore >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new FulfilmentConstraintException(
          "Product can be fulfilled by at most "
              + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE
              + " warehouses per store");
    }

    long warehousesForStore = fulfilmentRepository.countDistinctWarehousesByStore(storeId);
    if (warehousesForStore >= MAX_WAREHOUSES_PER_STORE
        && !fulfilmentRepository.isWarehouseUsedForStore(storeId, warehouseId)) {
      throw new FulfilmentConstraintException(
          "Store can be fulfilled by at most " + MAX_WAREHOUSES_PER_STORE + " warehouses");
    }

    long productTypesForWarehouse =
        fulfilmentRepository.countDistinctProductsByWarehouse(warehouseId);
    if (productTypesForWarehouse >= MAX_PRODUCT_TYPES_PER_WAREHOUSE) {
      throw new FulfilmentConstraintException(
          "Warehouse can store at most " + MAX_PRODUCT_TYPES_PER_WAREHOUSE + " product types");
    }

    StoreProductFulfilment assignment = new StoreProductFulfilment(storeId, productId, warehouseId);
    fulfilmentRepository.persist(assignment);
    LOGGER.infov(
        "Fulfilment assigned: storeId={0}, productId={1}, warehouseId={2}",
        storeId, productId, warehouseId);
  }

  /** Removes the assignment if present. Idempotent. */
  @Transactional
  public void unassign(Long storeId, Long productId, Long warehouseId) {
    StoreProductFulfilment existing =
        fulfilmentRepository.findByStoreAndProductAndWarehouse(storeId, productId, warehouseId);
    if (existing != null) {
      fulfilmentRepository.delete(existing);
      LOGGER.infov(
          "Fulfilment unassigned: storeId={0}, productId={1}, warehouseId={2}",
          storeId, productId, warehouseId);
    }
  }

  public List<StoreProductFulfilment> listByStore(Long storeId) {
    return fulfilmentRepository.listByStore(storeId);
  }

  public List<StoreProductFulfilment> listByWarehouse(Long warehouseId) {
    return fulfilmentRepository.listByWarehouse(warehouseId);
  }
}
