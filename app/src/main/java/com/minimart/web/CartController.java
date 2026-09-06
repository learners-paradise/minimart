package com.minimart.web;

import com.minimart.catalog.Product;
import com.minimart.catalog.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    private final Cart cart;
    private final ProductService productService;

    public CartController(Cart cart, ProductService productService) {
        this.cart = cart;
        this.productService = productService;
    }

    @PostMapping("/cart/add")
    public String add(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity) {
        cart.add(productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String remove(@RequestParam Long productId) {
        cart.remove(productId);
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String view(Model model) {
        List<CartLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : cart.getItems().entrySet()) {
            Product product = productService.get(entry.getKey());
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
            lines.add(new CartLine(product, entry.getValue(), subtotal));
            total = total.add(subtotal);
        }

        model.addAttribute("lines", lines);
        model.addAttribute("total", total);
        return "cart";
    }
}
