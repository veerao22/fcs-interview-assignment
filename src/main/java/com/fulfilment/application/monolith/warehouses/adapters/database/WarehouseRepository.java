package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOGGER = Logger.getLogger(WarehouseRepository.class.getName());

  @Override
  public List<Warehouse> getAll() {
    return this.list("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    LOGGER.infov("Creating warehouse with business unit code: {0}", warehouse.businessUnitCode);
    DbWarehouse entity = new DbWarehouse();
    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt;
    entity.archivedAt = warehouse.archivedAt;
    persist(entity);
  }

  @Override
  public void update(Warehouse warehouse) {
    LOGGER.infov("Updating warehouse with business unit code: {0}", warehouse.businessUnitCode);
    DbWarehouse entity = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (entity == null) {
      LOGGER.warnv("Warehouse not found for update: {0}", warehouse.businessUnitCode);
      return;
    }
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt;
    entity.archivedAt = warehouse.archivedAt;
    persist(entity);
  }

  @Override
  public void remove(Warehouse warehouse) {
    LOGGER.infov("Removing warehouse with business unit code: {0}", warehouse.businessUnitCode);
    DbWarehouse entity = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (entity != null) {
      delete(entity);
    } else {
      LOGGER.warnv("Warehouse not found for removal: {0}", warehouse.businessUnitCode);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    LOGGER.infov("Finding active warehouse by business unit code: {0}", buCode);
    DbWarehouse entity = find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    if (entity == null) {
      return null;
    }
    return entity.toWarehouse();
  }

  @Override
  public Warehouse getById(Long id) {
    if (id == null) {
      return null;
    }
    DbWarehouse entity = findById(id);
    if (entity == null || entity.archivedAt != null) {
      return null;
    }
    return entity.toWarehouse();
  }

  @Override
  public long countActiveByLocation(String location) {
    return count("location = ?1 and archivedAt is null", location);
  }

  @Override
  public int totalCapacityByLocation(String location) {
    Long sum =
        getEntityManager()
            .createQuery(
                "select coalesce(sum(w.capacity), 0) from DbWarehouse w where w.location = ?1 and w.archivedAt is null",
                Long.class)
            .setParameter(1, location)
            .getSingleResult();
    return sum != null ? sum.intValue() : 0;
  }
}
