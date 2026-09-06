class CheckoutPage {
  constructor(page) {
    this.page = page;
    this.nameInput = page.getByTestId('customer-name-input');
    this.emailInput = page.getByTestId('customer-email-input');
    this.placeOrderButton = page.getByTestId('place-order-button');
    this.errorBanner = page.getByTestId('checkout-error');
  }

  async fillDetails(name, email) {
    await this.nameInput.fill(name);
    await this.emailInput.fill(email);
  }

  async submit() {
    await this.placeOrderButton.click();
  }
}

module.exports = { CheckoutPage };
