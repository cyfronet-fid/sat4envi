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
import {EntityStore, EntityUIStore, StoreConfig} from '@datorama/akita';
import {COLLAPSED_CATEGORIES_LOCAL_STORAGE_KEY, createProductState, Product, ProductState, ProductUIState} from './product.model';
import {LocalStorage} from '../../../../app.providers';
import {yyyymmdd} from '../../../../utils/miscellaneous/date-utils';


@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Product'})
export class ProductStore extends EntityStore<ProductState, Product> {
  public readonly ui: EntityUIStore<ProductUIState>;

  constructor(@Inject(LocalStorage) storage: Storage) {
    super(createProductState());
    this.createUIStore({collapsedCategories: JSON.parse(storage.getItem(COLLAPSED_CATEGORIES_LOCAL_STORAGE_KEY)) || []}).setInitialEntityState({isLoading: false, isFavouriteLoading: false});
  }

  setSelectedDate(dateString: string, manualTrigger: boolean = false) {
    const date = new Date(dateString);

    this.update(store => ({
      ...store,
      ui: {
        ...store.ui,
        selectedMonth: date.getMonth(),
        selectedYear: date.getFullYear(),
        selectedDay: date.getDate(),
        selectedDate: yyyymmdd(date),
        manuallySelectedDate: manualTrigger ? dateString : store.ui.manuallySelectedDate
      }
    }));
  }
}

