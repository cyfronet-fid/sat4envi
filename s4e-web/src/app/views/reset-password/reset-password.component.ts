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

import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {SessionService} from '../../state/session/session.service';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {filter, map, switchMap} from 'rxjs/operators';

@Component({
  selector: 's4e-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnDestroy {
  sendTokenForm = new FormGroup<{email: string}>({
    email: new FormControl('', [Validators.required, Validators.email])
  });
  resetPasswordForm = new FormGroup<{password: string}>({
    password: new FormControl<string>(null, [Validators.required, Validators.minLength(8)])
  });

  token$ = this._activatedRoute.paramMap
    .pipe(
      map(paramMap => paramMap.has('token')
        && !!paramMap.get('token')
        && paramMap.get('token')
        || null
      )
    );

  constructor(
    private _activatedRoute: ActivatedRoute,
    private _router: Router,
    private _sessionService: SessionService
  ) {}

  resetPassword() {
    if (this.resetPasswordForm.invalid) {
      return;
    }

    this.token$
      .pipe(
        untilDestroyed(this),
        filter(token => !!token),
        switchMap(token => this._sessionService
          .resetPassword$(token, this.resetPasswordForm.value.password)
          .pipe(untilDestroyed(this))
        )
      )
      .subscribe(() => {
        this.sendTokenForm.reset();
        this._router.navigateByUrl('/login')
      });
  }

  sendPasswordResetToken() {
    if (this.sendTokenForm.invalid) {
      return;
    }

    this._sessionService.sendPasswordResetToken$(this.sendTokenForm.value.email)
      .pipe(untilDestroyed(this))
      .subscribe(() => this.sendTokenForm.reset());
  }

  ngOnDestroy(): void {}
}
