package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  private static final String PATH = "product";

  // --- GET list ---
  @Test
  public void get_returnsListOfProducts() {
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(containsString("KALLAX"), containsString("BESTÅ"));
  }

  // --- GET single ---
  @Test
  public void getSingle_whenExists_returnsProduct() {
    given()
        .when()
        .get(PATH + "/2")
        .then()
        .statusCode(200)
        .body(containsString("KALLAX"))
        .body("id", notNullValue())
        .body("name", notNullValue());
  }

  @Test
  public void getSingle_whenNotExists_returns404() {
    given()
        .when()
        .get(PATH + "/99999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  // --- POST create ---
  @Test
  public void create_whenIdSet_returns422() {
    given()
        .contentType("application/json")
        .body("{\"id\": 1, \"name\": \"NEW_PROD\", \"stock\": 0}")
        .when()
        .post(PATH)
        .then()
        .statusCode(422)
        .body(containsString("Id was invalidly set"));
  }

  @Test
  public void create_whenValid_returns201() {
    given()
        .contentType("application/json")
        .body("{\"name\": \"CREATED_PRODUCT\", \"stock\": 10}")
        .when()
        .post(PATH)
        .then()
        .statusCode(201)
        .body(containsString("CREATED_PRODUCT"));
  }

  // --- PUT update ---
  @Test
  public void update_whenExists_returns200() {
    given()
        .contentType("application/json")
        .body("{\"name\": \"UPDATED_KALLAX\", \"description\": \"desc\", \"stock\": 7}")
        .when()
        .put(PATH + "/2")
        .then()
        .statusCode(200)
        .body(containsString("UPDATED_KALLAX"));
  }

  @Test
  public void update_whenNotExists_returns404() {
    given()
        .contentType("application/json")
        .body("{\"name\": \"ANY\", \"stock\": 0}")
        .when()
        .put(PATH + "/99999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  @Test
  public void update_whenNameNull_returns422() {
    given()
        .contentType("application/json")
        .body("{\"stock\": 5}")
        .when()
        .put(PATH + "/2")
        .then()
        .statusCode(422)
        .body(containsString("Product Name was not set"));
  }

  // --- DELETE ---
  @Test
  public void delete_whenExists_returns204() {
    long id =
        ((Number)
                given()
                    .contentType("application/json")
                    .body("{\"name\": \"TO_DELETE_PROD\", \"stock\": 0}")
                    .when()
                    .post(PATH)
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id"))
            .longValue();
    given().when().delete(PATH + "/" + id).then().statusCode(204);
  }

  @Test
  public void delete_whenNotExists_returns404() {
    given()
        .when()
        .delete(PATH + "/99999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  // --- Original CRUD flow ---
  @Test
  public void testCrudProduct() {
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    given().when().delete(PATH + "/1").then().statusCode(204);

    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }
}
