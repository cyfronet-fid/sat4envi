/*
 * Copyright 2020 ACC Cyfronet AGH
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
