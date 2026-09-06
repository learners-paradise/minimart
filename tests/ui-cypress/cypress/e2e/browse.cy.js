describe('Browsing products', () => {
  beforeEach(() => {
    cy.visit('/products');
  });

  it('shows a grid of products by default', () => {
    cy.getByTestId('product-grid').should('be.visible');
    cy.get('[data-testid^="product-card-"]').should('have.length.greaterThan', 0);
  });

  it('filtering by category only shows matching products', () => {
    cy.getByTestId('category-select').select('Books');
    cy.url().should('include', 'category=Books');

    cy.get('[data-testid^="product-card-"]').should('have.length.greaterThan', 0);
    cy.get('[data-testid^="product-card-"] .category').each(($el) => {
      expect($el.text()).to.eq('Books');
    });
  });

  it('searching narrows results to matching product names', () => {
    cy.getByTestId('search-input').type('keyboard{enter}');

    cy.get('[data-testid^="product-card-"] h3').should('have.length.greaterThan', 0);
    cy.get('[data-testid^="product-card-"] h3').each(($el) => {
      expect($el.text().toLowerCase()).to.include('keyboard');
    });
  });

  it('an unmatched search shows a no-results message', () => {
    cy.getByTestId('search-input').type('nonexistent-product-zzz{enter}');
    cy.getByTestId('no-results').should('be.visible');
  });

  it('pagination moves to the next page of results', () => {
    cy.get('[data-testid^="product-card-"] h3').first().invoke('text').then((firstPageFirstName) => {
      cy.getByTestId('next-page').click();
      cy.url().should('include', 'page=1');

      cy.get('[data-testid^="product-card-"] h3').first().invoke('text').should('not.eq', firstPageFirstName);
    });
  });
});
