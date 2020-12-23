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

import {from, of} from 'rxjs';
import {mapAnyTrue, filterTrue, filterFalse, filterNull, filterNotNull, mapAllTrue} from './observable';
import {take, toArray} from 'rxjs/operators';
import {filterNil} from '@datorama/akita';

describe('isAnyTrue', () => {
  it('should work for arrays', async () => {
    expect(await from([
      [false, false, false],
      [true, true, false]
    ]).pipe(mapAnyTrue(), toArray()).toPromise()).toEqual([false, true]);
  });

  it('should work for booleans', async () => {
    expect(await from([
      false,
      true
    ]).pipe(mapAnyTrue(), toArray()).toPromise()).toEqual([false, true]);
  });
});

describe('mapAllTrue', () => {
  it('should work', async () => {
    expect(await from([
      [true, false, null],
      [true, true, true]
    ]).pipe(mapAllTrue(), toArray()).toPromise())
      .toEqual([false, true])
  });
});

describe('filterTrue', () => {
  it('should work', async () => {
    expect(await from([false, null, undefined, true, {}, NaN]).pipe(filterTrue(), toArray()).toPromise())
      .toEqual([true, {}])
  });
});

describe('filterFalse', () => {
  it('should work', async () => {
    expect(await from([false, null, undefined, true, {}, NaN]).pipe(filterFalse(), toArray()).toPromise())
      .toEqual([false, null, undefined, NaN])
  });
});

describe('filterNull', () => {
  it('should work', async () => {
    expect(await from([false, null, undefined, true, {}, NaN]).pipe(filterNull(), toArray()).toPromise())
      .toEqual([null, undefined])
  });
});


describe('filterNotNull', () => {
  it('should work', async () => {
    expect(await from([false, null, undefined, true, {}, NaN]).pipe(filterNotNull(), toArray()).toPromise())
      .toEqual([false, true, {}, NaN])
  });
});
