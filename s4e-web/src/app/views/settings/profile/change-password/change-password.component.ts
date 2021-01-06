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

import {NotificationService} from 'notifications';
import {Component} from '@angular/core';
import {GenericFormComponent} from 'src/app/utils/miscellaneous/generic-form.component';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from 'src/app/state/form/form.model';
import {Router} from '@angular/router';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {SessionQuery} from '../../../../state/session/session.query';
import {PasswordChangeFormState} from '../../../../state/session/session.model';
import {SessionService} from '../../../../state/session/session.service';

@Component({
  selector: 's4e-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent extends GenericFormComponent<SessionQuery, PasswordChangeFormState> {
  constructor(
    fm: AkitaNgFormsManager<FormState>,
    router: Router,
    _sessionQuery: SessionQuery,
    private _sessionService: SessionService,
    private _notificationService: NotificationService,
    private _router: Router
  ) {
    super(fm, router, _sessionQuery, 'resetPassword');
  }

  ngOnInit(): void {
    this.form = new FormGroup<PasswordChangeFormState>({
      oldPassword: new FormControl<string>(null, [Validators.required, Validators.minLength(8)]),
      newPassword: new FormControl<string>(null, [Validators.required, Validators.minLength(8)])
    });
    super.ngOnInit();
  }

  submitPasswordChange() {
    if (this.form.valid) {
      this._sessionService
        .changePassword(
          this.form.controls.oldPassword.value,
          this.form.controls.newPassword.value
        )
        .pipe(untilDestroyed(this))
        .subscribe(() => this.reset());
    }
  }

  async reset() {
    this.form.reset();
    this._sessionService.clearError();
    await this._router.navigate(
      ['/settings/profile'],
      {
        queryParamsHandling: 'merge'
      }
    );
  }
}
