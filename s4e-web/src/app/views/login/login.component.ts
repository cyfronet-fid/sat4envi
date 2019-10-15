import {Component, ViewEncapsulation} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../state/form/form.model';
import {validateAllFormFields} from '../../utils/miscellaneous/miscellaneous';
import {SessionQuery} from '../../state/session/session.query';
import {SessionService} from '../../state/session/session.service';
import {LoginFormState} from '../../state/session/session.model';
import {Router} from '@angular/router';
import {GenericFormComponent} from '../../utils/miscellaneous/generic-form.component';

@Component({
  selector: 's4e-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class LoginComponent extends GenericFormComponent<SessionQuery, LoginFormState> {
  constructor(fm: AkitaNgFormsManager<FormState>,
              router: Router,
              private sessionService: SessionService,
              private sessionQuery: SessionQuery) {
    super(fm, router, sessionQuery, 'login');
  }

  ngOnInit() {
    this.form = new FormGroup<LoginFormState>({
      login: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required]),
      rememberMe: new FormControl(false)
    });

    super.ngOnInit();
  }

  login() {
    validateAllFormFields(this.form);

    if (!this.form.valid) { return; }

    this.sessionService.login(this.form.controls.login.value, this.form.controls.password.value);
  }
}
