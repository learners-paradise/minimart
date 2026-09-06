package com.minimart.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ProductApiTest extends BaseApiTest {

    @Test
    void listsProductsWithDefaultPagination() {
        given()
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("content", not(empty()))
            .body("content.size()", lessThanOrEqualTo(20))
            .body("number", equalTo(0));
    }

    @Test
    void filtersProductsByCategory() {
        given()
            .queryParam("category", "Books")
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("content", not(empty()))
            .body("content.category", everyItem(equalTo("Books")));
    }

    @Test
    void searchesProductsByName() {
        given()
            .queryParam("search", "keyboard")
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("content.name", everyItem(containsStringIgnoringCase("keyboard")));
    }

    @Test
    void respectsPageSizeParameter() {
        given()
            .queryParam("size", 5)
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("content.size()", equalTo(5))
            .body("size", equalTo(5));
    }

    @Test
    void getsSingleProductById() {
        Long productId = given()
            .queryParam("size", 1)
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .extract().jsonPath().getLong("content[0].id");

        given()
        .when()
            .get("/api/products/{id}", productId)
        .then()
            .statusCode(200)
            .body("id", equalTo(productId.intValue()))
            .body("name", not(emptyOrNullString()))
            .body("price", notNullValue());
    }

    @Test
    void returns404ForUnknownProduct() {
        given()
        .when()
            .get("/api/products/{id}", 999_999)
        .then()
            .statusCode(404)
            .body("status", equalTo(404))
            .body("error", equalTo("Not Found"));
    }

    @Test
    void createsProductWithValidPayload() {
        String uniqueSku = "TEST-" + System.currentTimeMillis();

        given()
            .contentType("application/json")
            .body("""
                {
                  "sku": "%s",
                  "name": "API Test Product",
                  "description": "Created by an automated API test",
                  "category": "Electronics",
                  "price": 9.99,
                  "stockQuantity": 10
                }
                """.formatted(uniqueSku))
        .when()
            .post("/api/products")
        .then()
            .statusCode(201)
            .body("sku", equalTo(uniqueSku))
            .body("name", equalTo("API Test Product"))
            .body("id", notNullValue());
    }

    @Test
    void rejectsProductWithMissingRequiredFields() {
        given()
            .contentType("application/json")
            .body("""
                {
                  "name": "",
                  "price": -5,
                  "stockQuantity": -1
                }
                """)
        .when()
            .post("/api/products")
        .then()
            .statusCode(400)
            .body("error", equalTo("Validation Failed"))
            .body("details", not(empty()));
    }
}
