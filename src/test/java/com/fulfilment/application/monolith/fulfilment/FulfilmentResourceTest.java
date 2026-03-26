package com.fulfilment.application.monolith.fulfilment;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * REST API tests for fulfilment. Constraint validation (max 2 wh/product/store, max 3 wh/store, max
 * 5 products/warehouse) is covered by {@link FulfilmentServiceTest}.
 */
@QuarkusTest
class FulfilmentResourceTest {

  @Nested
  @DisplayName("Assign")
  class Assign {

    @Test
    void validAssign_returns204() {
      given()
          .contentType(MediaType.APPLICATION_JSON)
          .when()
          .post("fulfilment/store/1/product/1/warehouse/1")
          .then()
          .statusCode(204);
    }
  }

  @Nested
  @DisplayName("Unassign")
  class Unassign {

    @Test
    void unassign_returns204() {
      given().when().delete("fulfilment/store/3/product/1/warehouse/2").then().statusCode(204);
    }
  }

  @Nested
  @DisplayName("List")
  class List {

    @Test
    void listByStoreId_returns200() {
      given().queryParam("storeId", 1).when().get("fulfilment").then().statusCode(200);
    }

    @Test
    void listByWarehouseId_returns200() {
      given().queryParam("warehouseId", 1).when().get("fulfilment").then().statusCode(200);
    }
  }
}
