package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.exceptions.LocationIdentifierInvalidException;
import com.fulfilment.application.monolith.exceptions.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class LocationGatewayTest {

  @Inject
  LocationGateway locationGateway;

  @Test
  public void testWhenResolveNonExistentLocationShouldThrowNotFoundException() {
    assertThrows(
            LocationNotFoundException.class, () -> locationGateway.resolveByIdentifier("UNKNOWN-999"));
  }

  @Test
  public void testWhenResolveNullIdentifierShouldThrowInvalidException() {
    assertThrows(
            LocationIdentifierInvalidException.class, () -> locationGateway.resolveByIdentifier(null));
  }

  @Test
  public void testWhenResolveBlankIdentifierShouldThrowInvalidException() {
    assertThrows(
            LocationIdentifierInvalidException.class, () -> locationGateway.resolveByIdentifier("  "));
  }

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  public void testWhenResolveAnotherExistingLocationShouldReturn() {
    Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");

    assertEquals("AMSTERDAM-001", location.identification);
    assertEquals(5, location.maxNumberOfWarehouses);
    assertEquals(100, location.maxCapacity);
  }
}
