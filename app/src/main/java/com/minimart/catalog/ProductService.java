package com.minimart.catalog;

import com.minimart.common.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> list(String category, String search, Pageable pageable) {
        boolean hasCategory = StringUtils.hasText(category);
        boolean hasSearch = StringUtils.hasText(search);

        if (hasCategory && hasSearch) {
            return productRepository.findByCategoryIgnoreCaseAndNameContainingIgnoreCase(category, search, pageable);
        }
        if (hasCategory) {
            return productRepository.findByCategoryIgnoreCase(category, pageable);
        }
        if (hasSearch) {
            return productRepository.findByNameContainingIgnoreCase(search, pageable);
        }
        return productRepository.findAll(pageable);
    }

    public Product get(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public void decreaseStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }
}
