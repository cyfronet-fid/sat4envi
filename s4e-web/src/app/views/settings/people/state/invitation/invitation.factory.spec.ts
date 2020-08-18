import * as Factory from 'factory.ts';
import { Invitation } from './invitation.model';

export const InvitationFactory = Factory.makeFactory<Invitation>({
  email: Factory.each(i => `email#${i}@mail.pl`),
  token: Factory.each(i => `token-${i}`),
  status: 'waiting'
});
