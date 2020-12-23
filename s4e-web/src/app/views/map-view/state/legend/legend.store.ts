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

import {Inject, Injectable} from '@angular/core';
import {Store, StoreConfig} from '@datorama/akita';
import {COLLAPSED_LEGEND_LOCAL_STORAGE_KEY, LegendState} from './legend.model';
import {LocalStorage} from '../../../../app.providers';

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Legend' })
export class LegendStore extends Store<LegendState> {
  constructor(@Inject(LocalStorage) storage: Storage) {
    super({
      legend: null,
      isOpen: JSON.parse(storage.getItem(COLLAPSED_LEGEND_LOCAL_STORAGE_KEY) || 'false')
    });
  }
}

