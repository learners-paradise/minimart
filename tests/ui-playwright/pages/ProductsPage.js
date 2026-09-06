class ProductsPage {
  constructor(page) {
    this.page = page;
    this.categorySelect = page.getByTestId('category-select');
    this.searchInput = page.getByTestId('search-input');
    this.productGrid = page.getByTestId('product-grid');
    this.nextPageLink = page.getByTestId('next-page');
    this.prevPageLink = page.getByTestId('prev-page');
    this.noResults = page.getByTestId('no-results');
  }

  async goto() {
    await this.page.goto('/products');
  }

  async filterByCategory(category) {
    await this.categorySelect.selectOption({ label: category });
  }

  async search(term) {
    await this.searchInput.fill(term);
    await this.searchInput.press('Enter');
  }

  productCard(productId) {
    return this.page.getByTestId(`product-card-${productId}`);
  }

  async openFirstProduct() {
    await this.page.getByTestId('view-product-link').first().click();
  }
}

module.exports = { ProductsPage };
