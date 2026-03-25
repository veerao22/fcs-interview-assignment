package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.excpetions.DuplicateBusinessUnitCodeException;
import com.fulfilment.application.monolith.warehouses.domain.excpetions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class WarehouseResourceImplTest {

  @InjectMock
  WarehouseRepository warehouseRepository;

  @InjectMock
  CreateWarehouseOperation createWarehouseOperation;

  @InjectMock
  ReplaceWarehouseOperation replaceWarehouseOperation;

  @InjectMock
  ArchiveWarehouseOperation archiveWarehouseOperation;

  @Inject
  WarehouseResource warehouseResource;

  private static com.warehouse.api.beans.Warehouse apiWarehouse(
          String buCode, String location, Integer capacity, Integer stock) {
    var w = new com.warehouse.api.beans.Warehouse();
    w.setBusinessUnitCode(buCode);
    w.setLocation(location);
    w.setCapacity(capacity);
    w.setStock(stock);
    return w;
  }

  private static Warehouse domainWarehouse(
          String buCode, String location, Integer capacity, Integer stock) {
    var w = new Warehouse();
    w.businessUnitCode = buCode;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    return w;
  }

  @Test
  public void listAllWarehousesUnits_returnsMappedListFromRepository() {
    var d1 = domainWarehouse("MWH.001", "ZWOLLE-001", 100, 10);
    var d2 = domainWarehouse("MWH.012", "AMSTERDAM-001", 50, 5);
    when(warehouseRepository.getAll()).thenReturn(List.of(d1, d2));

    List<com.warehouse.api.beans.Warehouse> result = warehouseResource.listAllWarehousesUnits();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("MWH.001", result.get(0).getBusinessUnitCode());
    assertEquals("ZWOLLE-001", result.get(0).getLocation());
    assertEquals(100, result.get(0).getCapacity());
    assertEquals(10, result.get(0).getStock());
    assertEquals("MWH.012", result.get(1).getBusinessUnitCode());
    assertEquals("AMSTERDAM-001", result.get(1).getLocation());
    verify(warehouseRepository).getAll();
  }

  @Test
  public void listAllWarehousesUnits_returnsEmptyListWhenRepositoryReturnsEmpty() {
    when(warehouseRepository.getAll()).thenReturn(List.of());

    List<com.warehouse.api.beans.Warehouse> result = warehouseResource.listAllWarehousesUnits();

    assertNotNull(result);
    assertEquals(0, result.size());
    verify(warehouseRepository).getAll();
  }

  @Test
  public void createANewWarehouseUnit_callsUseCaseWithMappedDomainAndReturnsMappedResponse() {
    var request = apiWarehouse("MWH.NEW", "AMSTERDAM-001", 30, 5);

    var response = warehouseResource.createANewWarehouseUnit(request);

    ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
    verify(createWarehouseOperation).create(captor.capture());
    var passed = captor.getValue();
    assertEquals("MWH.NEW", passed.businessUnitCode);
    assertEquals("AMSTERDAM-001", passed.location);
    assertEquals(30, passed.capacity);
    assertEquals(5, passed.stock);

    assertNotNull(response);
    assertEquals("MWH.NEW", response.getBusinessUnitCode());
    assertEquals("AMSTERDAM-001", response.getLocation());
    assertEquals(30, response.getCapacity());
    assertEquals(5, response.getStock());
  }

  @Test
  public void createANewWarehouseUnit_returnsResponseWithSameDataAsRequest() {
    var request = apiWarehouse("MWH.TEST", "TILBURG-001", 40, 20);

    var response = warehouseResource.createANewWarehouseUnit(request);

    assertNotNull(response);
    assertEquals("MWH.TEST", response.getBusinessUnitCode());
    assertEquals("TILBURG-001", response.getLocation());
    assertEquals(40, response.getCapacity());
    assertEquals(20, response.getStock());
    verify(createWarehouseOperation).create(any(Warehouse.class));
  }

  @Test
  public void createANewWarehouseUnit_whenUseCaseThrowsDuplicateBuCode_exceptionPropagates() {
    var request = apiWarehouse("MWH.001", "AMSTERDAM-001", 30, 5);
    doThrow(new DuplicateBusinessUnitCodeException("MWH.001"))
            .when(createWarehouseOperation)
            .create(any(Warehouse.class));

    assertThrows(
            DuplicateBusinessUnitCodeException.class,
            () -> warehouseResource.createANewWarehouseUnit(request));

    verify(createWarehouseOperation).create(any(Warehouse.class));
  }

  @Test
  public void getAWarehouseUnitByID_returnsWarehouseWhenFound() {
    var warehouse = domainWarehouse("MWH.001", "ZWOLLE-001", 100, 10);
    when(warehouseRepository.getById(1L)).thenReturn(warehouse);

    var response = warehouseResource.getAWarehouseUnitByID("1");

    assertNotNull(response);
    assertEquals("MWH.001", response.getBusinessUnitCode());
    assertEquals("ZWOLLE-001", response.getLocation());
    assertEquals(100, response.getCapacity());
    assertEquals(10, response.getStock());
    verify(warehouseRepository).getById(1L);
  }

  @Test
  public void getAWarehouseUnitByID_throwsWhenWarehouseNotFound() {
    when(warehouseRepository.getById(999L)).thenReturn(null);

    assertThrows(
            WarehouseNotFoundException
                    .class,
            () -> warehouseResource.getAWarehouseUnitByID("999"));

    verify(warehouseRepository).getById(999L);
  }

  @Test
  public void getAWarehouseUnitByID_throwsWhenIdIsNotNumeric() {
    assertThrows(
            WarehouseNotFoundException
                    .class,
            () -> warehouseResource.getAWarehouseUnitByID("abc"));
  }

  @Test
  public void archiveAWarehouseUnitByID_callsGetByIdAndArchiveWhenFound() {
    var warehouse = domainWarehouse("MWH.001", "ZWOLLE-001", 100, 10);
    when(warehouseRepository.getById(1L)).thenReturn(warehouse);

    warehouseResource.archiveAWarehouseUnitByID("1");

    verify(warehouseRepository).getById(1L);
    verify(archiveWarehouseOperation).archive(warehouse);
  }

  @Test
  public void archiveAWarehouseUnitByID_throwsWhenWarehouseNotFound() {
    when(warehouseRepository.getById(999L)).thenReturn(null);

    assertThrows(
            WarehouseNotFoundException
                    .class,
            () -> warehouseResource.archiveAWarehouseUnitByID("999"));

    verify(warehouseRepository).getById(999L);
    verify(archiveWarehouseOperation, never()).archive(any());
  }

  @Test
  public void replaceTheCurrentActiveWarehouse_callsUseCaseAndReturnsMappedResponse() {
    var data = apiWarehouse("MWH.001", "AMSTERDAM-001", 80, 50);

    var response = warehouseResource.replaceTheCurrentActiveWarehouse("MWH.001", data);

    ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
    verify(replaceWarehouseOperation).replace(captor.capture());
    var passed = captor.getValue();
    assertEquals("MWH.001", passed.businessUnitCode);
    assertEquals("AMSTERDAM-001", passed.location);
    assertEquals(80, passed.capacity);
    assertEquals(50, passed.stock);

    assertNotNull(response);
    assertEquals("MWH.001", response.getBusinessUnitCode());
    assertEquals("AMSTERDAM-001", response.getLocation());
    assertEquals(80, response.getCapacity());
    assertEquals(50, response.getStock());
  }
}
