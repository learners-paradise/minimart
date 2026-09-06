package com.minimart.web;

import com.minimart.catalog.Product;

import java.math.BigDecimal;

public record CartLine(Product product, int quantity, BigDecimal subtotal) {
}
