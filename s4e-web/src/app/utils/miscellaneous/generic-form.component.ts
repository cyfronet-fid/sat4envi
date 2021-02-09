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

import {OnDestroy, OnInit, Directive} from '@angular/core';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../state/form/form.model';
import {NavigationEnd, Router} from '@angular/router';
import {devRestoreFormState} from './miscellaneous';
import {debounceTime, filter} from 'rxjs/operators';
import {connectErrorsToForm} from './forms';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {Query} from '@datorama/akita';
import {FormGroup} from '@ng-stack/forms';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';

@UntilDestroy()
@Directive()
export class GenericFormComponent<Q extends Query<any>, FS extends object>
  implements OnInit, OnDestroy {
  public loading$: Observable<boolean>;
  public form: FormGroup<FS> = null;
  public error$: Observable<any>;

  constructor(
    protected fm: AkitaNgFormsManager<FormState>,
    protected router: Router,
    protected query: Q,
    protected formKey: keyof FormState
  ) {}

  ngOnInit(): void {
    if (this.form == null) {
      throw new Error(
        'GenericFormComponent has no defined `form`, be sure to assign it before calling `super.ngOnInit()`'
      );
    }

    this.loading$ = this.query.selectLoading();
    this.error$ = this.query.selectError();

    if (environment.hmr) {
      devRestoreFormState(this.fm.query.getValue()[this.formKey], this.form);
      this.fm.upsert(this.formKey, this.form);

      // In order for dev error setting to work debounceTime(100) must be set
      this.query
        .selectError()
        .pipe(debounceTime(100), untilDestroyed(this))
        .subscribe(errors => connectErrorsToForm(errors, this.form));

      this.router.events
        .pipe(filter(event => event instanceof NavigationEnd))
        .subscribe(() => this.fm.remove(this.formKey));
    }
  }

  ngOnDestroy(): void {
    if (environment.hmr) {
      this.fm.unsubscribe(this.formKey);
    }
  }
}
