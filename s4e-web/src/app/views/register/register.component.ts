import {Component, OnDestroy, OnInit} from '@angular/core';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../state/form/form.model';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {RegisterFormState} from './state/register.model';
import {RegisterQuery} from './state/register.query';
import {Observable} from 'rxjs';
import {RegisterService} from './state/register.service';
import {devRestoreFormState, validateAllFormFields} from '../../utils/miscellaneous/miscellaneous';

@Component({
  selector: 's4e-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit, OnDestroy {
  public form: FormGroup<RegisterFormState>;
  public error$: Observable<any>;
  public loading$: Observable<boolean>;


  constructor(private fm: AkitaNgFormsManager<FormState>,
              private registerService: RegisterService,
              private registerQuery: RegisterQuery) { }

  ngOnInit() {
    this.form = new FormGroup<RegisterFormState>({
      login: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required]),
      passwordRepeat: new FormControl('', [Validators.required, () => {
        if (this.form == null || this.form.controls.passwordRepeat == null || this.form.controls.password == null) {
          return null;
        }

        if (this.form.controls.passwordRepeat.value !== this.form.controls.password.value) {
          return {notSame: true};
        }
        return null;
      }]),
    });

    this.error$ = this.registerQuery.selectError();
    this.loading$ = this.registerQuery.selectLoading();

    devRestoreFormState(this.fm.query.getValue().register, this.form);

    this.fm.upsert('register', this.form);
  }

  register() {
    validateAllFormFields(this.form);

    if (!this.form.valid) { return; }

    this.registerService.register(this.form.controls.login.value, this.form.controls.password.value);
  }

  ngOnDestroy(): void {
    this.fm.unsubscribe('register');
  }
}
