describe('Cart', () => {
  it('shows an empty state when nothing has been added', () => {
    cy.visit('/cart');
    cy.getByTestId('empty-cart').should('be.visible');
  });

  it('adding a product from its detail page shows it in the cart with the right subtotal', () => {
    cy.visit('/products');
    cy.getByTestId('view-product-link').first().click();

    cy.url().then((url) => {
      const productId = url.split('/').pop();

      cy.getByTestId('product-price').invoke('text').then((priceText) => {
        const unitPrice = parseFloat(priceText.replace('$', ''));

        cy.getByTestId('quantity-input').clear().type('2');
        cy.getByTestId('add-to-cart-button').click();

        cy.url().should('include', '/cart');

        const expectedSubtotal = (unitPrice * 2).toFixed(2);
        cy.get(`[data-testid="cart-row-${productId}"]`)
          .should('be.visible')
          .and('contain.text', `$${expectedSubtotal}`);
      });
    });
  });

  it('removing the only item in the cart returns it to the empty state', () => {
    cy.visit('/products');
    cy.getByTestId('view-product-link').first().click();

    cy.url().then((url) => {
      const productId = url.split('/').pop();

      cy.getByTestId('add-to-cart-button').click();
      cy.get(`[data-testid="cart-row-${productId}"]`).should('be.visible');

      cy.get(`[data-testid="cart-row-${productId}"]`).within(() => {
        cy.getByTestId('remove-item-button').click();
      });

      cy.getByTestId('empty-cart').should('be.visible');
    });
  });
});
