const { test, expect } = require('@playwright/test');
const { ProductsPage } = require('../pages/ProductsPage');
const { ProductDetailPage } = require('../pages/ProductDetailPage');
const { CartPage } = require('../pages/CartPage');
const { CheckoutPage } = require('../pages/CheckoutPage');

test.describe('Checkout', () => {
  test('golden path: browse, add to cart, check out, see confirmation', async ({ page }) => {
    const products = new ProductsPage(page);
    await products.goto();
    await products.openFirstProduct();

    const detail = new ProductDetailPage(page);
    const productName = await detail.name.textContent();
    await detail.addToCart(1);

    const cart = new CartPage(page);
    await cart.proceedToCheckout();
    await expect(page).toHaveURL('/checkout');

    const checkout = new CheckoutPage(page);
    const uniqueEmail = `playwright-${Date.now()}@example.com`;
    await checkout.fillDetails('Playwright Golden Path', uniqueEmail);
    await checkout.submit();

    await expect(page).toHaveURL(/\/orders\/\d+\/confirmation/);
    await expect(page.getByTestId('order-confirmation')).toBeVisible();
    await expect(page.getByTestId('order-id')).toBeVisible();
    await expect(page.locator('main')).toContainText(productName);
  });

  test('ordering more than the available stock surfaces a server-side error, not a silent failure', async ({ page }) => {
    const products = new ProductsPage(page);
    await products.goto();
    await products.openFirstProduct();

    const detail = new ProductDetailPage(page);
    const stockText = await detail.stock.textContent(); // e.g. "42 in stock"
    const availableStock = parseInt(stockText, 10);

    await detail.addToCart(availableStock + 1000);

    const cart = new CartPage(page);
    await cart.proceedToCheckout();

    const checkout = new CheckoutPage(page);
    await checkout.fillDetails('Overordering Buyer', `overorder-${Date.now()}@example.com`);
    await checkout.submit();

    await expect(page).toHaveURL('/checkout');
    await expect(checkout.errorBanner).toBeVisible();
    await expect(checkout.errorBanner).toContainText(/insufficient stock/i);
  });
});
