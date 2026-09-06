package com.minimart.integration;

import com.minimart.MiniMartApplication;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Boots the real Spring context against a throwaway Postgres container (not a mock,
 * not H2) so tests exercise actual constraints, transactions and Flyway migrations —
 * with no HTTP layer and no browser involved.
 *
 * <p>Uses the Testcontainers "singleton container" pattern: the container is started
 * once in a static initializer and shared (and never explicitly stopped) across every
 * test class that extends this base, instead of being annotated with {@code @Container}
 * — which would have JUnit stop it after the first test class finishes, breaking every
 * class that runs after it.
 */
@SpringBootTest(classes = MiniMartApplication.class)
@Tag("integration")
public abstract class IntegrationTestBase {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("minimart")
            .withUsername("minimart")
            .withPassword("minimart");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
