import { RemoteConfiguration } from 'src/app/utils/initializer/config.service';
import { Component, ViewEncapsulation, OnInit } from '@angular/core';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../state/form/form.model';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {appUsageTypes, RegisterFormState, scientificDomainsTypes} from './state/register.model';
import {RegisterQuery} from './state/register.query';
import {RegisterService} from './state/register.service';
import {validateAllFormFields} from '../../utils/miscellaneous/miscellaneous';
import {ActivatedRoute, Router} from '@angular/router';
import {GenericFormComponent} from '../../utils/miscellaneous/generic-form.component';
import {map} from 'rxjs/operators';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {countries} from './state/countries';

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
  public scientificDomainTypes = scientificDomainsTypes;
  public appUsageTypes = appUsageTypes;
  public countries = countries;

  constructor(fm: AkitaNgFormsManager<FormState>,
              router: Router,
              private activatedRoute: ActivatedRoute,
              public remoteConfiguration: RemoteConfiguration,
              private registerService: RegisterService,
              private registerQuery: RegisterQuery) {
    super(fm, router, registerQuery, 'register');
  }

  ngOnInit() {
    this.form = new FormGroup<RegisterFormState>({
      email: new FormControl('', [Validators.required, Validators.email]),
      name: new FormControl('', [Validators.required]),
      surname: new FormControl('', Validators.required),
      password: new FormControl('', [Validators.required, Validators.minLength(8)]),
      passwordRepeat: new FormControl('', [Validators.required, Validators.minLength(8)]),
      domain: new FormControl('', [Validators.required]),
      usage: new FormControl<string>('', [Validators.required]),
      country: new FormControl<string>('', [Validators.required]),
      policy: new FormControl<boolean>(false, [Validators.required, Validators.requiredTrue]),
      recaptcha: new FormControl('', [Validators.required])
    }, {validators: MustMatch('password', 'passwordRepeat')});

    super.ngOnInit();
  }

  register() {
    validateAllFormFields(this.form, {formKey: this.formKey, fm: this.fm});
    if (!this.form.valid) {
      return;
    }

    this.activatedRoute.queryParams
      .pipe(
        untilDestroyed(this),
        map(params => params.token)
      )
      .subscribe((token: string | null) => {
        let {recaptcha, passwordRepeat, ...request} = this.form.value;
        this.registerService.register(request, recaptcha, token);
      });
  }
}
