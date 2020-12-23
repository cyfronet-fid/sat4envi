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

import {Injectable} from '@angular/core';
import {QueryEntity, EntityStore} from '@datorama/akita';
import {SearchResultsState} from './search-result.model';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SearchResultsQuery<T> extends QueryEntity<SearchResultsState<T>, T> {

  constructor(protected store: EntityStore<SearchResultsState<T>, T>) {
    super(store);
  }

  selectIsOpen(): Observable<boolean> {
    return this.select(state  => state.isOpen);
  }

  selectLocation(): Observable<T | null> {
    return this.select(state  => state.searchResult);
  }

}
