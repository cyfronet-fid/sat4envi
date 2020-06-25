import { RegisterFormState } from './state/register.model';
import * as Factory from 'factory.ts';

export const RegisterFactory = Factory.makeFactory<RegisterFormState>({
  email: Factory.each(i => `email#${i}@mail.pl`),
  password: Factory.each(i => `password#${i}`),
  passwordRepeat: Factory.each(i => `password#${i}`),
  recaptcha: Factory.each(i => `captcha#${i}`),
  surname: Factory.each(i => `surname#${i}`),
  name: Factory.each(i => `name#${i}`),
});
