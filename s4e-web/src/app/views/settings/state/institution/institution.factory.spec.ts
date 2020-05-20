import { Institution } from './institution.model';
import * as Factory from 'factory.ts';

export const InstitutionFactory = Factory.makeFactory<Institution>({
  id: Factory.each(i => `institution:${i}`),

  parentInstitutionName: 'test#2',
  parentInstitutionSlug: 'test-2',

  name: Factory.each(i => `test #${i}`),
  slug: 'test-1',
  address: 'address',
  postalCode: '00-000',
  city: 'city',
  phone: '+48 000 000 000',
  secondaryPhone: '+48 000 000 000',
  emblem: 'logo_um.png',
  institutionAdminEmail: 'zkMember@mail.pl'
});
