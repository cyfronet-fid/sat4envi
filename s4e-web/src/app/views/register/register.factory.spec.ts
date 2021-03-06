/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {RegisterFormState} from './state/register.model';
import * as Factory from 'factory.ts';

export const RegisterFactory = Factory.makeFactory<RegisterFormState>({
  email: Factory.each(i => `email#${i}@mail.pl`),
  password: Factory.each(i => `password#${i}`),
  passwordRepeat: Factory.each(i => `password#${i}`),
  recaptcha: Factory.each(i => `captcha#${i}`),
  policy: true,
  domain: 'LAND',
  usage: 'COMMERCIAL',
  country: 'PL',
  surname: Factory.each(i => `surname#${i}`),
  name: Factory.each(i => `name#${i}`)
});
