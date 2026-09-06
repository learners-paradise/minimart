package com.minimart.integration;

import com.minimart.catalog.Product;
import com.minimart.catalog.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductRepositoryIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ProductRepository productRepository;

    private Product newProduct(String sku) {
        Product product = new Product();
        product.setSku(sku);
        product.setName("Integration Test Product");
        product.setDescription("Created by an integration test");
        product.setCategory("Electronics");
        product.setPrice(new BigDecimal("19.99"));
        product.setStockQuantity(5);
        return product;
    }

    @Test
    @Transactional
    void savesAndReadsBackAProductFromTheRealDatabase() {
        Product saved = productRepository.save(newProduct("ITEST-" + System.nanoTime()));

        Product found = productRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("Integration Test Product");
        assertThat(found.getPrice()).isEqualByComparingTo("19.99");
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    @Transactional
    void enforcesUniqueSkuConstraintAtTheDatabaseLevel() {
        String sku = "ITEST-DUP-" + System.nanoTime();
        productRepository.save(newProduct(sku));
        productRepository.flush();

        assertThatThrownBy(() -> {
            productRepository.save(newProduct(sku));
            productRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void categoryFilterQueryOnlyReturnsMatchingRows() {
        var page = productRepository.findByCategoryIgnoreCase("Books", PageRequest.of(0, 100));

        assertTrue(page.getTotalElements() > 0);
        assertThat(page.getContent()).allSatisfy(p -> assertThat(p.getCategory()).isEqualToIgnoringCase("Books"));
    }
}
