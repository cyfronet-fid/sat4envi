import {Component, OnDestroy, OnInit} from '@angular/core';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../state/form/form.model';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {RegisterFormState} from './state/register.model';
import {RegisterQuery} from './state/register.query';
import {Observable} from 'rxjs';
import {RegisterService} from './state/register.service';
import {connectErrorsToForm, devRestoreFormState, validateAllFormFields} from '../../utils/miscellaneous/miscellaneous';
import {HashMap} from '@datorama/akita';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {debounceTime, delay} from 'rxjs/operators';

export function MustMatch(controlName: string, matchingControlName: string) {
  return (formGroup: FormGroup) => {
    const control = formGroup.controls[controlName];
    const matchingControl = formGroup.controls[matchingControlName];

    if (matchingControl.errors && !matchingControl.errors['mustMatch']) {
      // return if another validator has already found an error on the matchingControl
      return;
    }

    // set error on matchingControl if validation fails
    if (control.value !== matchingControl.value) {
      matchingControl.setErrors({ mustMatch: true });
    } else {
      matchingControl.setErrors(null);
    }
  }
}

@Component({
  selector: 's4e-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit, OnDestroy {
  public form: FormGroup<RegisterFormState>;
  public error: HashMap<string> = {};
  public loading$: Observable<boolean>;


  constructor(private fm: AkitaNgFormsManager<FormState>,
              private registerService: RegisterService,
              private registerQuery: RegisterQuery) { }

  ngOnInit() {
    this.form = new FormGroup<RegisterFormState>({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required]),
      passwordRepeat: new FormControl('', [Validators.required]),
    }, {validators: MustMatch('password', 'passwordRepeat')});

    this.loading$ = this.registerQuery.selectLoading();

    devRestoreFormState(this.fm.query.getValue().register, this.form);
    this.fm.upsert('register', this.form);

    // In order for dev error setting to work debounceTime(100) must be set
    this.registerQuery.selectError().pipe(debounceTime(100), untilDestroyed(this)).subscribe(errors => {
      this.error = errors;
      connectErrorsToForm(errors, this.form);
    });
  }

  register() {
    validateAllFormFields(this.form);

    if (!this.form.valid) { return; }

    this.registerService.register(this.form.controls.email.value, this.form.controls.password.value);
  }

  ngOnDestroy(): void {
    this.fm.unsubscribe('register');
  }
}
