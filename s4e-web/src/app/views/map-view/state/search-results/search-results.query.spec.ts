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

import {SearchResultsQuery} from './search-results.query';
import {EntityStore} from '@datorama/akita';
import {SearchResultsState} from './search-result.model';

describe('SearchResultsQuery', () => {
  let query: SearchResultsQuery<any>;

  beforeEach(() => {
    query = new SearchResultsQuery<any>(
      new EntityStore<SearchResultsState<any>, any>()
    );
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });
});
