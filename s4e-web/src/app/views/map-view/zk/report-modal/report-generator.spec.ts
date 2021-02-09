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

import {ReportGenerator} from './report-generator';
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {HttpClient} from '@angular/common/http';
import {take, toArray} from 'rxjs/operators';
import {of} from 'rxjs';

describe('ReportGenerator', () => {
  let generator: ReportGenerator;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    http = TestBed.inject(HttpClient);
    generator = new ReportGenerator(http);
  });

  it('working$ and loading$ should be set properly', async function () {
    expect(await generator.loading$.pipe(take(1)).toPromise()).toBe(true);
    expect(await generator.working$.pipe(take(1)).toPromise()).toBe(false);
  });

  it('loadAssets should load fonts and images', async function () {
    let s1 = spyOn(generator as any, 'loadFont').and.returnValue(
      of(true).toPromise()
    );
    let s2 = spyOn(generator as any, 'loadImage').and.returnValue(
      of(true).toPromise()
    );

    generator.loadAssets();

    expect(s1).toHaveBeenCalledTimes(2);
    // expect(s1.calls[0]).toEqual(import('./fonts/Ubuntu-Regular-normal'));
    // expect(s1.calls[1]).toEqual(import('./fonts/Ubuntu-Regular-bold'));
    expect(s2).toHaveBeenCalledTimes(2);
    // expect(s2.calls[0]).toEqual('asd');
    // expect(s2.calls[1]).toEqual('asd');
    expect(await generator.loading$.pipe(take(2), toArray()).toPromise()).toEqual([
      true,
      false
    ]);
  });
});
