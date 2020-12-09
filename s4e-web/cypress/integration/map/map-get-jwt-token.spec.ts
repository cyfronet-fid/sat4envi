import { GeneralModal } from './../../page-objects/modal/general-modal.po';
/// <reference types="Cypress" />

import { Map } from '../../page-objects/map/map.po';
import { Login } from '../../page-objects/auth/auth-login.po';
import { MapProducts } from '../../page-objects/map/map-products.po';
import { Core } from './../../page-objects/core.po';

// describe.skip('Map favorite products', () => {
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
//       .goToApiManula()
//   })
// });
