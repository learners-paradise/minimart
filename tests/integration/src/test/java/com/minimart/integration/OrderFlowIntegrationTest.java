package com.minimart.integration;

import com.minimart.catalog.Product;
import com.minimart.catalog.ProductRepository;
import com.minimart.common.BadRequestException;
import com.minimart.orders.Order;
import com.minimart.orders.OrderService;
import com.minimart.orders.dto.CreateOrderRequest;
import com.minimart.orders.dto.OrderItemRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderFlowIntegrationTest extends IntegrationTestBase {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    private Product seedProduct(int stock) {
        Product product = new Product();
        product.setSku("ITEST-ORDER-" + System.nanoTime());
        product.setName("Order Flow Test Product");
        product.setCategory("Electronics");
        product.setPrice(new BigDecimal("10.00"));
        product.setStockQuantity(stock);
        return productRepository.saveAndFlush(product);
    }

    @Test
    void placingAnOrderDecreasesStockInTheDatabase() {
        Product product = seedProduct(10);

        orderService.placeOrder(new CreateOrderRequest(
                "Integration Buyer", "integration@example.com",
                List.of(new OrderItemRequest(product.getId(), 3))
        ));

        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        assertThat(reloaded.getStockQuantity()).isEqualTo(7);
    }

    @Test
    void orderTotalIsCalculatedFromRealProductPrices() {
        Product product = seedProduct(10);

        Order order = orderService.placeOrder(new CreateOrderRequest(
                "Integration Buyer", "integration2@example.com",
                List.of(new OrderItemRequest(product.getId(), 4))
        ));

        assertThat(order.getTotalAmount()).isEqualByComparingTo("40.00");
    }

    @Test
    void multiItemOrderRollsBackStockChangesWhenOneItemFailsValidation() {
        Product okProduct = seedProduct(10);
        Product lowStockProduct = seedProduct(1);

        CreateOrderRequest request = new CreateOrderRequest(
                "Integration Buyer", "integration3@example.com",
                List.of(
                        new OrderItemRequest(okProduct.getId(), 2),
                        new OrderItemRequest(lowStockProduct.getId(), 5) // exceeds available stock
                )
        );

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");

        // Because placeOrder is @Transactional, the stock decrement for okProduct
        // (processed before the failing item) must be rolled back too.
        Product reloadedOkProduct = productRepository.findById(okProduct.getId()).orElseThrow();
        assertThat(reloadedOkProduct.getStockQuantity()).isEqualTo(10);
    }
}
