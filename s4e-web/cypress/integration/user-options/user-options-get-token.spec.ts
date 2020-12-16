
/// <reference types="Cypress" />
import { GeneralModal } from '../../page-objects/modal/general-modal.po';
import { MapDateSelect } from '../../page-objects/map/map-date-select.po';
import { Login } from '../../page-objects/auth/auth-login.po';
import { MapFavorities } from '../../page-objects/map/map-favoritie-products.po';
import { Core } from '../../page-objects/core.po';

// describe.skip('Get JWT Token', () => {
//   beforeEach(function () {
//     cy.fixture('users/zkMember.json').as('zkMember');
//   });

//   it('should get jwt token with good password', function () {
//     Login
//       .loginAs(this.zkMember)
//       .openJwtTokenModal()
//       .authenticateAs(this.zkMember)
//       .shouldHaveVisibleToken()
//       .tokenShouldContain('.')
//       .goToApiManual()
//   })
// });
