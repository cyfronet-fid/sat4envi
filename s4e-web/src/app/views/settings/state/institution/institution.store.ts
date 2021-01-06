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

import {Injectable} from '@angular/core';
import {ActiveState, EntityState, EntityStore, StoreConfig} from '@datorama/akita';
import {Institution} from './institution.model';


export interface InstitutionState extends EntityState<Institution>, ActiveState<string> {}

@Injectable({providedIn: 'root'})
@StoreConfig({
  name: 'Institution',
  idKey: 'slug',
  cache: {ttl: 60}
})
export class InstitutionStore extends EntityStore<InstitutionState, Institution, string> {
  constructor() {
    super();
  }
}

