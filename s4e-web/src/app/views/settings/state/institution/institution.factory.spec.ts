import {Institution, InstitutionForm} from './institution.model';
import * as Factory from 'factory.ts';

export const InstitutionFactory = Factory.makeFactory<InstitutionForm>({
  id: Factory.each(i => `institution:${i}`),

  parentName : null,
  parentSlug: null,

  name: Factory.each(i => `test #${i}`),
  slug: Factory.each(i => `test-${i}`),
  address: 'address',
  postalCode: '00-000',
  city: 'city',
  phone: '+48 000 000 000',
  secondaryPhone: '+48 000 000 000',
  emblem: 'logo_um.png',
  adminsEmails: 'zkMember@mail.pl'
});
