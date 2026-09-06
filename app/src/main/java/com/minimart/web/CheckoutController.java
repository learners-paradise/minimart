package com.minimart.web;

import com.minimart.common.BadRequestException;
import com.minimart.orders.Order;
import com.minimart.orders.OrderService;
import com.minimart.orders.dto.CreateOrderRequest;
import com.minimart.orders.dto.OrderItemRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class CheckoutController {

    private final Cart cart;
    private final OrderService orderService;

    public CheckoutController(Cart cart, OrderService orderService) {
        this.cart = cart;
        this.orderService = orderService;
    }

    @GetMapping("/checkout")
    public String checkoutForm(Model model) {
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        return "checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(
            @RequestParam String customerName,
            @RequestParam String customerEmail,
            Model model
    ) {
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }

        List<OrderItemRequest> items = cart.getItems().entrySet().stream()
                .map(e -> new OrderItemRequest(e.getKey(), e.getValue()))
                .toList();

        try {
            Order order = orderService.placeOrder(new CreateOrderRequest(customerName, customerEmail, items));
            cart.clear();
            return "redirect:/orders/" + order.getId() + "/confirmation";
        } catch (BadRequestException ex) {
            model.addAttribute("error", ex.getMessage());
            return "checkout";
        }
    }

    @GetMapping("/orders/{id}/confirmation")
    public String confirmation(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.get(id));
        return "order-confirmation";
    }
}
