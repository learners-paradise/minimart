package com.minimart.web;

import com.minimart.catalog.Product;
import com.minimart.catalog.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CatalogViewController {

    private static final List<String> CATEGORIES = List.of("Books", "Electronics", "Home", "Sports", "Toys");
    private static final int PAGE_SIZE = 12;

    private final ProductService productService;

    public CatalogViewController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/products";
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<Product> productPage = productService.list(category, search, PageRequest.of(page, PAGE_SIZE));

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("search", search);
        return "products";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.get(id));
        return "product-detail";
    }
}
