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

import {activateMatcher} from './utils';
import {UrlSegment} from '@angular/router';

describe('utils', () => {
  it('activate matches correct url', () => {
    const urls = [
      new UrlSegment('activate', {}),
      new UrlSegment('4aaeb75b-73a9-4e8c-9dfc-02012fa8e2f4', {})
    ];
    const result = activateMatcher(urls);
    expect(result).toEqual(
      {
        'consumed': urls,
        'posParams':
          {
            token: urls[1]
          }
      }
    );
  });

  it('activate doesn\'t match incorrect token', () => {
    const urls = [
      new UrlSegment('activate', {}),
      new UrlSegment('4aaeb75b-73a9-4e8c-9df', {})
    ];
    const result = activateMatcher(urls);
    expect(result).toBe(null);
  });

  it('activate doesn\'t match incorrect path', () => {
    const urls = [
      new UrlSegment('confirm-email', {}),
      new UrlSegment('4aaeb75b-73a9-4e8c-9df', {})
    ];
    const result = activateMatcher(urls);
    expect(result).toBe(null);
  });

  it('activate doesn\'t match too long urls', () => {
    const urls = [
      new UrlSegment('activate', {}),
      new UrlSegment('4aaeb75b-73a9-4e8c-9dfc-02012fa8e2f4', {}),
      new UrlSegment('foo', {}),
    ];
    const result = activateMatcher(urls);
    expect(result).toBe(null);
  });
});
