import { Person } from './person.model';
import * as Factory from 'factory.ts';

export const PersonFactory = Factory.makeFactory<Person>({
  email: Factory.each(i => `email#${i}@mail.pl`),
  name: Factory.each(i => `name#${i}`),
  surname: Factory.each(i => `surname#${i}`),
  roles: []
});
