import * as Factory from 'factory.ts';
import { Invitation } from './invitation.model';

export const InvitationFactory = Factory.makeFactory<Invitation>({
  email: Factory.each(i => `email#${i}@mail.pl`),
  id: Factory.each(i => `${i}`),
  status: 'waiting'
});
