const { test, expect } = require('@playwright/test');
const { ProductsPage } = require('../pages/ProductsPage');
const { ProductDetailPage } = require('../pages/ProductDetailPage');
const { CartPage } = require('../pages/CartPage');

test.describe('Cart', () => {
  test('shows an empty state when nothing has been added', async ({ page }) => {
    const cart = new CartPage(page);
    await cart.goto();

    await expect(cart.emptyCart).toBeVisible();
  });

  test('adding a product from its detail page shows it in the cart with the right subtotal', async ({ page }) => {
    const products = new ProductsPage(page);
    await products.goto();
    await products.openFirstProduct();

    const detail = new ProductDetailPage(page);
    const productId = new URL(page.url()).pathname.split('/').pop();
    const unitPriceText = await detail.price.textContent(); // e.g. "$34.99"
    const unitPrice = parseFloat(unitPriceText.replace('$', ''));

    await detail.addToCart(2);
    // Tomcat may URL-rewrite the first response with a ;jsessionid= suffix before it
    // knows the client accepts cookies, so match the path rather than an exact URL.
    await expect(page).toHaveURL(/\/cart(;jsessionid=[^?#]*)?$/);

    const cart = new CartPage(page);
    const row = cart.rowFor(productId);
    await expect(row).toBeVisible();

    const expectedSubtotal = (unitPrice * 2).toFixed(2);
    await expect(row).toContainText(`$${expectedSubtotal}`);
  });

  test('removing the only item in the cart returns it to the empty state', async ({ page }) => {
    const products = new ProductsPage(page);
    await products.goto();
    await products.openFirstProduct();

    const detail = new ProductDetailPage(page);
    const productId = new URL(page.url()).pathname.split('/').pop();
    await detail.addToCart(1);

    const cart = new CartPage(page);
    await expect(cart.rowFor(productId)).toBeVisible();

    await cart.removeItem(productId);

    await expect(cart.emptyCart).toBeVisible();
  });
});
