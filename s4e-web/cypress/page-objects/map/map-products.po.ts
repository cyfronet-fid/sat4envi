import { Core } from '../core.po';

export class MapProducts extends Core {
   
    static pageObject = {

      getAllProductsInAllCategories: () => cy.get('[data-e2e="product-list"] [data-e2e="products__item"]'),
      getProductNavbar: () => cy.get('[data-e2e="product-list"]'),
      getProductsNameBtns: () => cy.get('[data-e2e="product-list"] [data-e2e="picker-item-label"]'),
    }

    static selectProductBy(partialName: string) {
        MapProducts
          .pageObject
          .getProductsNameBtns()
          .contains(partialName)
          .should("be.visible")
          .click();
        return MapProducts;
      }
    
      static selectNthProduct(number: number) {
        MapProducts
          .pageObject
          .getProductsNameBtns()
          .eq(number)
          .should("be.visible")
          .click();
        return MapProducts;
      }
    
      static productsCountShouldBe(count: number) {
        MapProducts
          .pageObject
          .getAllProductsInAllCategories()
          .should('have.length', count);
    
        return MapProducts;
      }
    

}