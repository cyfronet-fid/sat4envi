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

import {Inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LegendStore } from './legend.store';
import {COLLAPSED_LEGEND_LOCAL_STORAGE_KEY, Legend} from './legend.model';
import {LocalStorage} from '../../../../app.providers';

@Injectable({ providedIn: 'root' })
export class LegendService {
  constructor(private store: LegendStore,
              @Inject(LocalStorage) private storage: Storage,
              private http: HttpClient) {
  }

  set(legend: Legend) {
    this.store.update({legend});
  }

  toggleLegend() {
    this.store.update(state => {
      this.storage.setItem(COLLAPSED_LEGEND_LOCAL_STORAGE_KEY, JSON.stringify(!state.isOpen));
      return {...state, isOpen: !state.isOpen}
    });
  }
}
