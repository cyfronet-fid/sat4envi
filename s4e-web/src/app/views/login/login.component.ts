import {Component, OnDestroy, OnInit, ViewEncapsulation} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {Observable} from 'rxjs';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../state/form/form.model';
import {devRestoreFormState, validateAllFormFields} from '../../utils/miscellaneous/miscellaneous';
import {SessionQuery} from '../../state/session/session.query';
import {SessionService} from '../../state/session/session.service';
import {LoginFormState} from '../../state/session/session.model';

@Component({
  selector: 's4e-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class LoginComponent implements OnInit, OnDestroy {
  public form: FormGroup<LoginFormState>;
  public error$: Observable<any>;
  public loading$: Observable<boolean>;


  constructor(private fm: AkitaNgFormsManager<FormState>,
              private sessionService: SessionService,
              private sessionQuery: SessionQuery) { }

  ngOnInit() {
    this.form = new FormGroup<LoginFormState>({
      login: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required]),
      rememberMe: new FormControl(false)
    });

    this.error$ = this.sessionQuery.selectError();
    this.loading$ = this.sessionQuery.selectLoading();

    devRestoreFormState(this.fm.query.getValue().login, this.form);

    this.fm.upsert('login', this.form);
  }

  login() {
    validateAllFormFields(this.form);

    if (!this.form.valid) { return; }

    this.sessionService.login(this.form.controls.login.value, this.form.controls.password.value);
  }

  ngOnDestroy(): void {
    this.fm.unsubscribe('login');
  }
}
