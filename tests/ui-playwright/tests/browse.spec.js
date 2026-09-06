const { test, expect } = require('@playwright/test');
const { ProductsPage } = require('../pages/ProductsPage');

test.describe('Browsing products', () => {
  test('shows a grid of products by default', async ({ page }) => {
    const products = new ProductsPage(page);
    await products.goto();

    await expect(products.productGrid).toBeVisible();
    await expect(page.locator('[data-testid^="product-card-"]').first()).toBeVisible();
  });

  test('filtering by category only shows matching products', async ({ page }) => {
    const products = new ProductsPage(page);
    await products.goto();

    await products.filterByCategory('Books');
    await expect(page).toHaveURL(/category=Books/);

    const cards = page.locator('[data-testid^="product-card-"]');
    await expect(cards.first()).toBeVisible();
    const categories = await cards.locator('.category').allTextContents();
    for (const category of categories) {
      expect(category).toBe('Books');
    }
  });

  test('searching narrows results to matching product names', async ({ page }) => {
    const products = new ProductsPage(page);
    await products.goto();

    await products.search('keyboard');

    const names = await page.locator('[data-testid^="product-card-"] h3').allTextContents();
    expect(names.length).toBeGreaterThan(0);
    for (const name of names) {
      expect(name.toLowerCase()).toContain('keyboard');
    }
  });

  test('an unmatched search shows a no-results message', async ({ page }) => {
    const products = new ProductsPage(page);
    await products.goto();

    await products.search('nonexistent-product-zzz');

    await expect(products.noResults).toBeVisible();
  });

  test('pagination moves to the next page of results', async ({ page }) => {
    const products = new ProductsPage(page);
    await products.goto();

    const firstPageFirstCard = await page.locator('[data-testid^="product-card-"] h3').first().textContent();

    await products.nextPageLink.click();
    await expect(page).toHaveURL(/page=1/);

    const secondPageFirstCard = await page.locator('[data-testid^="product-card-"] h3').first().textContent();
    expect(secondPageFirstCard).not.toBe(firstPageFirstCard);
  });
});
