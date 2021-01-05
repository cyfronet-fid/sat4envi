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
import {Store, StoreConfig} from '@datorama/akita';
import {COOKIE_POLICY_ACCEPTED_KEY, createSession, Session} from './session.model';
import {CookieService} from 'ngx-cookie-service';

@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Session'})
export class SessionStore extends Store<Session> {
  constructor(cookieService: CookieService) {
    super(
      createSession({
        cookiePolicyAccepted: cookieService.get(COOKIE_POLICY_ACCEPTED_KEY) === '1'
      })
    );
  }
}
