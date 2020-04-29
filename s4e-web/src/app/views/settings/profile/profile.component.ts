import { ProfileQuery } from './../../../state/profile/profile.query';
import { ProfileService } from './../../../state/profile/profile.service';
import {Component, OnInit} from '@angular/core';
import {SessionQuery} from '../../../state/session/session.query';
import {Observable} from 'rxjs';
import { FormGroup, FormControl, Validators } from '@ng-stack/forms';
import { GenericFormComponent } from 'src/app/utils/miscellaneous/generic-form.component';
import { PasswordChangeFormState } from 'src/app/state/profile/profile.model';
import { AkitaNgFormsManager } from '@datorama/akita-ng-forms-manager';
import { Router } from '@angular/router';
import { FormState } from 'src/app/state/form/form.model';

@Component({
  selector: 's4e-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent extends GenericFormComponent<ProfileQuery, PasswordChangeFormState> implements OnInit {
  public isLoggedIn$: Observable<boolean>;
  public userEmail$: Observable<string>;
  public form: FormGroup<PasswordChangeFormState>;

  constructor(
    fm: AkitaNgFormsManager<FormState>,
    router: Router,
    private _profileService: ProfileService,
    private _profileQuery: ProfileQuery,
    private _sessionQuery: SessionQuery

  ) {
    super(fm, router, _profileQuery, 'resetPassword');
  }

  ngOnInit(): void {
    this.isLoggedIn$ = this._sessionQuery.isLoggedIn$();
    this.userEmail$ = this._sessionQuery.select(state => state.email);

    this.form = new FormGroup<PasswordChangeFormState>({
      oldPassword: new FormControl('', [Validators.required]),
      newPassword: new FormControl('', [Validators.required])
    });
    super.ngOnInit();
  }

  submitPasswordChange() {
    if (this.form.valid) {
      this._profileService.resetPassword(
        this.form.controls.oldPassword.value,
        this.form.controls.newPassword.value
      );

      this.form.reset();
    }
  }
}
