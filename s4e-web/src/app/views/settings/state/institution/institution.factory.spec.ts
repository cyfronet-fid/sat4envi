import { Institution } from './institution.model';
import * as Factory from 'factory.ts';

export const InstitutionFactory = Factory.makeFactory<Institution>({
  id: Factory.each(i => `institution:${i}`),

  parentName : Factory.each(i => `parent #${i}`),
  parentSlug: Factory.each(i => `parent-${i}`),

  name: Factory.each(i => `test #${i}`),
  slug: Factory.each(i => `test-${i}`),
  address: 'address',
  postalCode: '00-000',
  city: 'city',
  phone: '+48 000 000 000',
  secondaryPhone: '+48 000 000 000',
  emblem: 'logo_um.png',
  institutionAdminEmail: 'zkMember@mail.pl'
});
