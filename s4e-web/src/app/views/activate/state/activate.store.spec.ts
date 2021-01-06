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

import {ActivateStore} from './activate.store';
import {ActivateQuery} from './activate.query';

describe('ActivateStore', () => {
  let store: ActivateStore;

  beforeEach(() => {
    store = new ActivateStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

  it('should update state', () => {
    store.setState('resending');
    const query = new ActivateQuery(store);
    expect(query.getValue().state).toBe('resending');
  });
});
