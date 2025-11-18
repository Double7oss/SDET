package com.example.fxdeals.integration;

import com.example.fxdeals.FxDealsApplication;
import com.example.fxdeals.core.DealRequest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
        classes = FxDealsApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public class DealsApiIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.4");

    @DynamicPropertySource
    static void dataSourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void preventsDuplicateAndAllowsPartialSuccess() {
        String id = UUID.randomUUID().toString();
        List<DealRequest> body = List.of(
                new DealRequest(id, "USD", "EUR", "2025-01-01T00:00:00Z", new BigDecimal("5.00")),
                new DealRequest(id, "USD", "EUR", "2025-01-01T00:00:00Z", new BigDecimal("5.00")),
                new DealRequest("not-uuid", "USD", "EUR", "2025-01-01T00:00:00Z", new BigDecimal("1.00"))
        );

        RestAssured.given().contentType(JSON).body(body)
                .when().post("/api/deals/import")
                .then().statusCode(200)
                .body("total", equalTo(3))
                .body("accepted", equalTo(1))
                .body("duplicates", equalTo(1))
                .body("rejected", equalTo(1));
    }
}
