package com.minimart.orders;

import com.minimart.catalog.Product;
import com.minimart.catalog.ProductService;
import com.minimart.common.BadRequestException;
import com.minimart.common.NotFoundException;
import com.minimart.orders.dto.CreateOrderRequest;
import com.minimart.orders.dto.OrderItemRequest;
import com.minimart.users.Customer;
import com.minimart.users.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final CustomerService customerService;

    public OrderService(OrderRepository orderRepository, ProductService productService, CustomerService customerService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.customerService = customerService;
    }

    @Transactional
    public Order placeOrder(CreateOrderRequest request) {
        Customer customer = customerService.findOrCreate(request.customerName(), request.customerEmail());

        Order order = new Order();
        order.setCustomer(customer);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productService.get(itemRequest.productId());

            if (product.getStockQuantity() < itemRequest.quantity()) {
                throw new BadRequestException(
                        "Insufficient stock for product '%s': requested %d, available %d"
                                .formatted(product.getName(), itemRequest.quantity(), product.getStockQuantity()));
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(itemRequest.quantity());
            order.addItem(item);

            total = total.add(item.getSubtotal());
            productService.decreaseStock(product, itemRequest.quantity());
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PLACED);
        return orderRepository.save(order);
    }

    public Order get(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    public Page<Order> list(String customerEmail, Pageable pageable) {
        if (StringUtils.hasText(customerEmail)) {
            return orderRepository.findByCustomerEmailIgnoreCase(customerEmail, pageable);
        }
        return orderRepository.findAll(pageable);
    }
}
