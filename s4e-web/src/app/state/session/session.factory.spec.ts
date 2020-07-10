import * as Factory from 'factory.ts';
import {Role, Session} from './session.model';

export const SessionFactory = Factory.makeFactory<Session>({
  email: 'admin@mail.pl',
  name: 'Admin',
  surname: 'Adminsky',
  roles: [],
  memberZK: true,
  admin: true
});

export const RoleFactory = Factory.makeFactory<Role>({
  groupSlug: Factory.each(i => `gr${i}`),
  institutionSlug: Factory.each(i => `inst${i}`),
  role: 'GROUP_MANAGER'
});
