import {Component, ViewEncapsulation} from '@angular/core';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../state/form/form.model';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {RegisterFormState} from './state/register.model';
import {RegisterQuery} from './state/register.query';
import {RegisterService} from './state/register.service';
import {validateAllFormFields} from '../../utils/miscellaneous/miscellaneous';
import {Router} from '@angular/router';
import {GenericFormComponent} from '../../utils/miscellaneous/generic-form.component';
import {S4eConfig} from '../../utils/initializer/config.service';

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
      matchingControl.setErrors({mustMatch: true});
    } else {
      matchingControl.setErrors(null);
    }
  };
}

@Component({
  selector: 's4e-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class RegisterComponent extends GenericFormComponent<RegisterQuery, RegisterFormState> {
  constructor(fm: AkitaNgFormsManager<FormState>,
              router: Router,
              public CONFIG: S4eConfig,
              private registerService: RegisterService,
              private registerQuery: RegisterQuery) {
    super(fm, router, registerQuery, 'register');
  }

  ngOnInit() {
    this.form = new FormGroup<RegisterFormState>({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required]),
      passwordRepeat: new FormControl('', [Validators.required]),
      recaptcha: new FormControl('', [Validators.required])
    }, {validators: MustMatch('password', 'passwordRepeat')});

    super.ngOnInit();
  }

  register() {
    validateAllFormFields(this.form);

    if (!this.form.valid) {
      return;
    }

    this.registerService.register(this.form.controls.email.value, this.form.controls.password.value, this.form.controls.recaptcha.value);
  }
}
