const { test, expect, request } = require('@playwright/test');

/**
 * Full-system golden-path journeys: real browser interaction through the UI, and then
 * cross-layer verification by calling the API directly to confirm the backend/DB state
 * actually changed the way the UI implied it did (order persisted, stock decremented).
 * These are deliberately few and cover only the critical paths — edge cases and
 * validation belong in the API/UI suites, not here.
 */

test.describe('Customer purchase journeys', () => {
  test('searches for a product, buys it, and the order + stock decrement are correct end-to-end', async ({ page, baseURL }) => {
    const api = await request.newContext({ baseURL });

    await page.goto('/products');
    await page.getByTestId('search-input').fill('mouse');
    await page.getByTestId('search-input').press('Enter');

    await page.getByTestId('view-product-link').first().click();

    const productId = new URL(page.url()).pathname.split('/').pop();
    const stockBefore = parseInt(await page.getByTestId('product-stock').textContent(), 10);

    await page.getByTestId('quantity-input').fill('1');
    await page.getByTestId('add-to-cart-button').click();

    await page.getByTestId('checkout-link').click();
    const email = `e2e-search-buy-${Date.now()}@example.com`;
    await page.getByTestId('customer-name-input').fill('E2E Journey Buyer');
    await page.getByTestId('customer-email-input').fill(email);
    await page.getByTestId('place-order-button').click();

    await expect(page).toHaveURL(/\/orders\/(\d+)\/confirmation/);
    const orderId = new URL(page.url()).pathname.match(/\/orders\/(\d+)\//)[1];

    // Cross-layer check: ask the API (source of truth) rather than trusting the UI alone.
    const orderResponse = await api.get(`/api/orders/${orderId}`);
    expect(orderResponse.ok()).toBeTruthy();
    const order = await orderResponse.json();
    expect(order.customerEmail).toBe(email);
    expect(order.items).toHaveLength(1);
    expect(order.items[0].quantity).toBe(1);

    const productResponse = await api.get(`/api/products/${productId}`);
    const product = await productResponse.json();
    expect(product.stockQuantity).toBe(stockBefore - 1);

    await api.dispose();
  });

  test('buys multiple different products in a single order', async ({ page, baseURL }) => {
    const api = await request.newContext({ baseURL });

    await page.goto('/products?category=Electronics');
    await page.getByTestId('view-product-link').first().click();
    const firstProductName = await page.getByTestId('product-name').textContent();
    await page.getByTestId('add-to-cart-button').click();

    await page.goto('/products?category=Books');
    await page.getByTestId('view-product-link').first().click();
    const secondProductName = await page.getByTestId('product-name').textContent();
    await page.getByTestId('add-to-cart-button').click();

    await page.getByTestId('checkout-link').click();
    const email = `e2e-multi-item-${Date.now()}@example.com`;
    await page.getByTestId('customer-name-input').fill('Multi Item Buyer');
    await page.getByTestId('customer-email-input').fill(email);
    await page.getByTestId('place-order-button').click();

    await expect(page).toHaveURL(/\/orders\/(\d+)\/confirmation/);
    const orderId = new URL(page.url()).pathname.match(/\/orders\/(\d+)\//)[1];

    const orderResponse = await api.get(`/api/orders/${orderId}`);
    const order = await orderResponse.json();

    expect(order.items).toHaveLength(2);
    const orderedProductNames = order.items.map((item) => item.productName);
    expect(orderedProductNames).toContain(firstProductName);
    expect(orderedProductNames).toContain(secondProductName);

    const expectedTotal = order.items.reduce((sum, item) => sum + item.subtotal, 0);
    expect(order.totalAmount).toBeCloseTo(expectedTotal, 2);

    await api.dispose();
  });
});
