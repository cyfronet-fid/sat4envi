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

import {BsDatepickerDirective} from 'ngx-bootstrap/datepicker';
import {BehaviorSubject} from 'rxjs';
import {filter, map} from 'rxjs/operators';
import moment from 'moment';

interface BsDatePickerStoreData {
  payload: {month: -1 | 1};
  type: string;
}

/**
 * This class encapsulates hacking private NGX-Bootstrap Datepicker's API
 * Which is required to catch 'change month' events.
 * As this functionality might break at any time it has been extracted into
 * separate class for ease of fixing.
 *
 * There is also Github issue https://github.com/valor-software/ngx-bootstrap/issues/1153
 * for this feature when it's finished this hack might be removed.
 */
export class BsDatePickerUtils {
  public viewChanged = new BehaviorSubject<Date>(undefined);

  constructor(private datePicker: BsDatepickerDirective) {}

  monthChanged$() {
    const store = ((this.datePicker as any)._datepicker.instance._store
      ._dispatcher as BehaviorSubject<BsDatePickerStoreData>).asObservable();
    return store.pipe(
      filter(data => data.type === '[datepicker] shift view date'),
      map(data =>
        moment(
          (this.datePicker as any)._datepicker.instance._store.source._value.view
            .date
        ).format('YYYY-MM')
      )
    );
  }
}
