package com.minimart.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class OrderApiTest extends BaseApiTest {

    private Long anyProductIdWithStock() {
        return given()
            .queryParam("size", 1)
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .extract().jsonPath().getLong("content[0].id");
    }

    @Test
    void placesOrderSuccessfully() {
        Long productId = anyProductIdWithStock();
        String email = "api-test-" + System.currentTimeMillis() + "@example.com";

        given()
            .contentType("application/json")
            .body("""
                {
                  "customerName": "API Test Buyer",
                  "customerEmail": "%s",
                  "items": [ { "productId": %d, "quantity": 2 } ]
                }
                """.formatted(email, productId))
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("status", equalTo("PLACED"))
            .body("customerEmail", equalTo(email))
            .body("items[0].quantity", equalTo(2))
            .body("totalAmount", notNullValue());
    }

    @Test
    void getsOrderById() {
        Long productId = anyProductIdWithStock();
        String email = "api-test-" + System.currentTimeMillis() + "@example.com";

        Integer orderId = given()
            .contentType("application/json")
            .body("""
                {
                  "customerName": "API Test Buyer",
                  "customerEmail": "%s",
                  "items": [ { "productId": %d, "quantity": 1 } ]
                }
                """.formatted(email, productId))
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .extract().jsonPath().getInt("id");

        given()
        .when()
            .get("/api/orders/{id}", orderId)
        .then()
            .statusCode(200)
            .body("id", equalTo(orderId))
            .body("customerEmail", equalTo(email));
    }

    @Test
    void returns404ForUnknownOrder() {
        given()
        .when()
            .get("/api/orders/{id}", 999_999)
        .then()
            .statusCode(404)
            .body("error", equalTo("Not Found"));
    }

    @Test
    void rejectsOrderExceedingAvailableStock() {
        Long productId = anyProductIdWithStock();

        given()
            .contentType("application/json")
            .body("""
                {
                  "customerName": "Overordering Buyer",
                  "customerEmail": "overorder@example.com",
                  "items": [ { "productId": %d, "quantity": 999999 } ]
                }
                """.formatted(productId))
        .when()
            .post("/api/orders")
        .then()
            .statusCode(400)
            .body("error", equalTo("Bad Request"))
            .body("message", containsStringIgnoringCase("insufficient stock"));
    }

    @Test
    void rejectsOrderForUnknownProduct() {
        given()
            .contentType("application/json")
            .body("""
                {
                  "customerName": "Ghost Buyer",
                  "customerEmail": "ghost@example.com",
                  "items": [ { "productId": 999999, "quantity": 1 } ]
                }
                """)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(404);
    }

    @Test
    void rejectsOrderWithInvalidEmail() {
        Long productId = anyProductIdWithStock();

        given()
            .contentType("application/json")
            .body("""
                {
                  "customerName": "Bad Email Buyer",
                  "customerEmail": "not-an-email",
                  "items": [ { "productId": %d, "quantity": 1 } ]
                }
                """.formatted(productId))
        .when()
            .post("/api/orders")
        .then()
            .statusCode(400)
            .body("error", equalTo("Validation Failed"));
    }

    @Test
    void rejectsOrderWithEmptyItems() {
        given()
            .contentType("application/json")
            .body("""
                {
                  "customerName": "No Items Buyer",
                  "customerEmail": "noitems@example.com",
                  "items": []
                }
                """)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(400)
            .body("error", equalTo("Validation Failed"));
    }
}
