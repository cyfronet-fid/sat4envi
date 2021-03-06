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

import {InstitutionQuery} from '../state/institution/institution.query';
import {Component} from '@angular/core';
import {SessionQuery} from '../../../state/session/session.query';
import {Observable} from 'rxjs';
import {Institution} from '../state/institution/institution.model';
import {SessionService} from '../../../state/session/session.service';
import {GenericFormComponent} from '../../../utils/miscellaneous/generic-form.component';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../state/form/form.model';
import {Router} from '@angular/router';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {validateAllFormFields} from '../../../utils/miscellaneous/miscellaneous';
import {ModalService} from '../../../modal/state/modal.service';
import {NotificationService} from '../../../notifications/state/notification.service';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';

@UntilDestroy()
@Component({
  selector: 's4e-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent extends GenericFormComponent<
  InstitutionQuery,
  {password: string}
> {
  public error$ = this._institutionQuery.selectError();
  public isLoading$ = this._institutionQuery.selectLoading();
  public userEmail$: Observable<string> = this._sessionQuery.select(
    state => state.email
  );
  public userName$: Observable<string> = this._sessionQuery.select(
    state => state.name
  );
  public userSurname$: Observable<string> = this._sessionQuery.select(
    state => state.surname
  );

  public institutions$ = this._institutionQuery.selectAll();

  form: FormGroup<{password: string}> = new FormGroup<{password: string}>({
    password: new FormControl<string>(null, [Validators.required])
  });

  constructor(
    private _formsManager: AkitaNgFormsManager<FormState>,
    private _router: Router,
    private _sessionQuery: SessionQuery,
    private _sessionService: SessionService,
    private _institutionQuery: InstitutionQuery,
    private _modalService: ModalService,
    private _notificationService: NotificationService
  ) {
    super(_formsManager, _router, _institutionQuery, 'removeUser');
  }

  isManagerOf(institution: Institution) {
    return this._institutionQuery.isManagerOf(institution);
  }

  async removeAccount() {
    validateAllFormFields(this.form, {formKey: this.formKey, fm: this.fm});
    if (this.form.invalid) {
      return;
    }

    const confirmAccountRemoval = await this._modalService.confirm(
      'Usuwanie konta',
      'Operacja usunięcia konta jest nieodwracalna. Czy napewno chcesz skasować konto?'
    );
    if (!confirmAccountRemoval) {
      return;
    }

    this._sessionService
      .removeAccount$(this._sessionQuery.getValue().email, this.form.value.password)
      .pipe(untilDestroyed(this))
      .subscribe(
        () => this._sessionService.logout(),
        () => {
          this._notificationService.addGeneral({
            content: 'Podałeś niepoprawne hasło lub utraciłeś sesję',
            type: 'error'
          });
          this.form.reset();
        }
      );
  }
}
