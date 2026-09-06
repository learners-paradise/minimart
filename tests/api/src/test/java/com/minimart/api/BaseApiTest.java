package com.minimart.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseApiTest {

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.baseURI = System.getProperty("api.baseUri", "http://localhost:8080");
    }
}
