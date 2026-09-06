class ProductDetailPage {
  constructor(page) {
    this.page = page;
    this.name = page.getByTestId('product-name');
    this.price = page.getByTestId('product-price');
    this.stock = page.getByTestId('product-stock');
    this.quantityInput = page.getByTestId('quantity-input');
    this.addToCartButton = page.getByTestId('add-to-cart-button');
  }

  async goto(productId) {
    await this.page.goto(`/products/${productId}`);
  }

  async addToCart(quantity = 1) {
    await this.quantityInput.fill(String(quantity));
    await this.addToCartButton.click();
  }
}

module.exports = { ProductDetailPage };
