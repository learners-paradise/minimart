describe('Checkout', () => {
  it('golden path: browse, add to cart, check out, see confirmation', () => {
    cy.visit('/products');
    cy.getByTestId('view-product-link').first().click();

    cy.getByTestId('product-name').invoke('text').then((productName) => {
      cy.getByTestId('add-to-cart-button').click();

      cy.getByTestId('checkout-link').click();
      cy.url().should('include', '/checkout');

      const uniqueEmail = `cypress-${Date.now()}@example.com`;
      cy.getByTestId('customer-name-input').type('Cypress Golden Path');
      cy.getByTestId('customer-email-input').type(uniqueEmail);
      cy.getByTestId('place-order-button').click();

      cy.url().should('match', /\/orders\/\d+\/confirmation/);
      cy.getByTestId('order-confirmation').should('be.visible');
      cy.getByTestId('order-id').should('be.visible');
      cy.get('main').should('contain.text', productName);
    });
  });

  it('ordering more than the available stock surfaces a server-side error, not a silent failure', () => {
    cy.visit('/products');
    cy.getByTestId('view-product-link').first().click();

    cy.getByTestId('product-stock').invoke('text').then((stockText) => {
      const availableStock = parseInt(stockText, 10);

      cy.getByTestId('quantity-input').clear().type(String(availableStock + 1000));
      cy.getByTestId('add-to-cart-button').click();

      cy.getByTestId('checkout-link').click();
      cy.getByTestId('customer-name-input').type('Overordering Buyer');
      cy.getByTestId('customer-email-input').type(`overorder-cy-${Date.now()}@example.com`);
      cy.getByTestId('place-order-button').click();

      cy.url().should('include', '/checkout');
      cy.getByTestId('checkout-error')
        .should('be.visible')
        .and('contain.text', 'Insufficient stock');
    });
  });
});
