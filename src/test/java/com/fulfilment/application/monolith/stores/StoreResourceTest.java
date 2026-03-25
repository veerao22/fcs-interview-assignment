package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class StoreResourceTest {

  @InjectMock LegacyStoreManagerGateway legacyStoreManagerGateway;

  @BeforeEach
  public void setup() {
    Mockito.reset(legacyStoreManagerGateway);
  }

  // --- GET list ---
  @Test
  public void get_returnsListOfStores() {
    given()
        .when()
        .get("store")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));
  }

  // --- GET single ---
  @Test
  public void getSingle_whenExists_returnsStore() {
    given()
        .when()
        .get("store/1")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"))
        .body("id", notNullValue())
        .body("name", notNullValue());
  }

  @Test
  public void getSingle_whenNotExists_returns404() {
    given().when().get("store/99999").then().statusCode(404).body(containsString("does not exist"));
  }

  // --- POST create ---
  @Test
  public void create_whenIdSet_returns422() {
    given()
        .contentType("application/json")
        .body("{\"id\": 1, \"name\": \"SOME\", \"quantityProductsInStock\": 0}")
        .when()
        .post("store")
        .then()
        .statusCode(422)
        .body(containsString("Id was invalidly set"));
  }

  @Test
  public void create_whenNameNull_returns422() {
    given()
        .contentType("application/json")
        .body("{\"quantityProductsInStock\": 0}")
        .when()
        .post("store")
        .then()
        .statusCode(422)
        .body(containsString("Store name is required"));
  }

  @Test
  public void create_whenNameBlank_returns422() {
    given()
        .contentType("application/json")
        .body("{\"name\": \"  \", \"quantityProductsInStock\": 0}")
        .when()
        .post("store")
        .then()
        .statusCode(422)
        .body(containsString("Store name is required"));
  }

  @Test
  public void testLegacyGatewayCalledOnSuccessfulCreate() {
    given()
        .contentType("application/json")
        .body("{\"name\": \"NEW_UNIQUE_STORE\", \"quantityProductsInStock\": 5}")
        .when()
        .post("store")
        .then()
        .statusCode(201)
        .body(containsString("NEW_UNIQUE_STORE"));

    Mockito.verify(legacyStoreManagerGateway).createStoreOnLegacySystem(Mockito.any());
  }

  @Test
  public void testLegacyGatewayNotCalledOnRollback() {
    // "TONSTAD" already exists in import.sql → duplicate name → 409 (or 500 if constraint fires).
    // No commit → legacy not called.
    given()
        .contentType("application/json")
        .body("{\"name\": \"TONSTAD\", \"quantityProductsInStock\": 99}")
        .when()
        .post("store");

    // AFTER_SUCCESS observer runs only after commit; duplicate must not trigger legacy sync
    Mockito.verify(legacyStoreManagerGateway, Mockito.never())
        .createStoreOnLegacySystem(Mockito.any());
  }

  @Test
  public void testLegacyGatewayCalledOnSuccessfulUpdate() {
    given()
        .contentType("application/json")
        .body("{\"name\": \"TONSTAD_UPDATED\", \"quantityProductsInStock\": 20}")
        .when()
        .put("store/1")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD_UPDATED"));

    Mockito.verify(legacyStoreManagerGateway).updateStoreOnLegacySystem(Mockito.any());
  }

  // --- PUT update ---
  @Test
  public void update_whenStoreNotExists_returns404() {
    given()
        .contentType("application/json")
        .body("{\"name\": \"ANY\", \"quantityProductsInStock\": 0}")
        .when()
        .put("store/99999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  @Test
  public void update_whenNameNull_returns422() {
    given()
        .contentType("application/json")
        .body("{\"quantityProductsInStock\": 10}")
        .when()
        .put("store/1")
        .then()
        .statusCode(422)
        .body(containsString("Store Name was not set"));
  }

  // --- PATCH ---
  @Test
  public void patch_whenExists_updatesAndReturns200() {
    given()
        .contentType("application/json")
        .body("{\"name\": \"PATCHED_NAME\", \"quantityProductsInStock\": 7}")
        .when()
        .patch("store/2")
        .then()
        .statusCode(200)
        .body(containsString("PATCHED_NAME"));
  }

  @Test
  public void patch_whenStoreNotExists_returns404() {
    given()
        .contentType("application/json")
        .body("{\"name\": \"ANY\", \"quantityProductsInStock\": 0}")
        .when()
        .patch("store/99999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  @Test
  public void patch_whenNameNull_returns422() {
    given()
        .contentType("application/json")
        .body("{\"quantityProductsInStock\": 5}")
        .when()
        .patch("store/1")
        .then()
        .statusCode(422)
        .body(containsString("Store Name was not set"));
  }

  // --- DELETE ---
  @Test
  public void delete_whenExists_returns204() {
    long id =
        ((Number)
                given()
                    .contentType("application/json")
                    .body("{\"name\": \"TO_DELETE\", \"quantityProductsInStock\": 0}")
                    .when()
                    .post("store")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id"))
            .longValue();
    given().when().delete("store/" + id).then().statusCode(204);
  }

  @Test
  public void delete_whenNotExists_returns404() {
    given()
        .when()
        .delete("store/99999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }
}
