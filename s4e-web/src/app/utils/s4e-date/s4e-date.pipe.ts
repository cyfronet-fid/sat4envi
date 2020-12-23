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

import {Inject, LOCALE_ID, Pipe} from '@angular/core';
import moment from 'moment';
import {DatePipe} from '@angular/common';
import environment from 'src/environments/environment';

@Pipe({
  name: 'S4EDate'
})
export class S4EDatePipe extends DatePipe {
  constructor(@Inject(LOCALE_ID) locale: string) {
    super(locale);
  }

  transform(value: any, args?: any): any {
    return value == null ? '' : super.transform(moment.utc(value, environment.backendDateFormat).toDate());
  }
}
