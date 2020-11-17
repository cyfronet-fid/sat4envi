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
    private _notificationService: NotificationService
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
        .resetPassword(
          this.form.controls.oldPassword.value,
          this.form.controls.newPassword.value
        )
        .pipe(untilDestroyed(this))
        .subscribe(() => {
          this._notificationService.addGeneral({
            type: 'success',
            content: 'Hasło zostało zmienione'
          });
          this.reset();
        });
    }
  }

  reset() {
    this.form.reset();
    this._sessionService.clearError();
  }
}
