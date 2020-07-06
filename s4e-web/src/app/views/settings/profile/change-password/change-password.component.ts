import { ProfileStore } from './../../../../state/profile/profile.store';
import { NotificationService } from './../../../../../../projects/notifications/src/lib/state/notification.service';
import { Component } from '@angular/core';
import { GenericFormComponent } from 'src/app/utils/miscellaneous/generic-form.component';
import { ProfileQuery } from 'src/app/state/profile/profile.query';
import { PasswordChangeFormState } from 'src/app/state/profile/profile.model';
import { AkitaNgFormsManager } from '@datorama/akita-ng-forms-manager';
import { FormState } from 'src/app/state/form/form.model';
import { Router } from '@angular/router';
import { ProfileService } from 'src/app/state/profile/profile.service';
import { FormGroup, Validators, FormControl } from '@ng-stack/forms';
import { untilDestroyed } from 'ngx-take-until-destroy';

@Component({
  selector: 's4e-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent extends GenericFormComponent<ProfileQuery, PasswordChangeFormState> {
  constructor(
    fm: AkitaNgFormsManager<FormState>,
    router: Router,
    private _profileService: ProfileService,
    private _profileQuery: ProfileQuery,
    private _profileStore: ProfileStore,
    private _notificationService: NotificationService
  ) {
    super(fm, router, _profileQuery, 'resetPassword');
  }

  ngOnInit(): void {
    this.form = new FormGroup<PasswordChangeFormState>({
      oldPassword: new FormControl<string>(null, [Validators.required, Validators.minLength(6)]),
      newPassword: new FormControl<string>(null, [Validators.required, Validators.minLength(6)])
    });
    super.ngOnInit();
  }

  submitPasswordChange() {
    if (this.form.valid) {
      this._profileService
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
    this._profileStore.setError(null);
  }
}
