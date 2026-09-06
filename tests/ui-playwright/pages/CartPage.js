class CartPage {
  constructor(page) {
    this.page = page;
    this.emptyCart = page.getByTestId('empty-cart');
    this.cartTable = page.getByTestId('cart-table');
    this.cartTotal = page.getByTestId('cart-total');
    this.checkoutLink = page.getByTestId('checkout-link');
  }

  async goto() {
    await this.page.goto('/cart');
  }

  rowFor(productId) {
    return this.page.getByTestId(`cart-row-${productId}`);
  }

  async removeItem(productId) {
    await this.rowFor(productId).getByTestId('remove-item-button').click();
  }

  async proceedToCheckout() {
    await this.checkoutLink.click();
  }
}

module.exports = { CartPage };
