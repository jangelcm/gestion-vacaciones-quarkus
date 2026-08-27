package com.example.gateway;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(TestBackendResource.class)
class GatewayRouteTest {

    @Test
    void proxiesRequestToBackend() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(equalTo("hello from backend"));
    }
}
