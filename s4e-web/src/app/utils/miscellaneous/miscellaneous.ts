import {JsonConvert} from 'json2typescript';
import {FormGroup, FormControl} from '@ng-stack/forms';
import {FormState} from '../../state/form/form.model';
import {AbstractControl} from '@angular/forms';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {environment} from '../../../environments/environment';
import {HashMap} from '@datorama/akita';
import {InjectorModule} from '../../common/injector.module';
import {IConstants, S4E_CONSTANTS} from '../../app.constants';

export type ServerApiError = HashMap<string[]>|{__exception__: string, __stacktrace__: string, __general__: string[]}

export function disableEnableForm(disable: boolean, form: FormGroup<any>|FormControl<any>) {
  if (disable) {
    form.disable();
  } else {
    form.enable();
  }
}


export function deserializeJsonResponse<T, Y extends T>(json: T, SerializationClass: {new(): Y}): T;
export function deserializeJsonResponse<T, Y extends T>(json: T[], SerializationClass: {new(): Y}): T[];
export function deserializeJsonResponse<T, Y extends T>(json: T|T[], SerializationClass: {new(): Y}): T|T[];
export function deserializeJsonResponse<T, Y extends T>(json: any, SerializationClass: {new(): Y}): T;
export function deserializeJsonResponse<T, Y extends T>(json: any[], SerializationClass: {new(): Y}): T[];
export function deserializeJsonResponse<T, Y extends T>(json: any|any[], SerializationClass: {new(): Y}): T|T[] {
  const converter = new JsonConvert();
  const out = converter.deserialize(json, SerializationClass);
  if (out instanceof  Array) {
    return out.map(element => Object.assign({}, element));
  }
  return Object.assign({}, out);
}

export function connectErrorsToForm(errors: ServerApiError|null, form: FormGroup) {
  const CONSTANTS: IConstants = InjectorModule.Injector.get(S4E_CONSTANTS);

  if(errors == null) {
    form.setErrors({});
    return;
  }

  Object.entries(errors).filter(([key, value]: [string, string[]]) => form.get(key) != null)
    // .map(([key, value]: [string, string[]]) => [key, value.reduce((pv, cv, i) => {pv[`server_${i}`] = cv; return pv}, {})])
    .forEach(([key, value]: [string, string[]]) => {
      if(key === CONSTANTS.generalErrorKey) {
        form.setErrors({[CONSTANTS.generalErrorKey]: value}, {emitEvent: false})
      } else {
        form.get(key).setErrors({server: value}, {emitEvent: true})
      }
    });
}

export function validateAllFormFields(formGroup: FormGroup<any>) {
  Object.keys(formGroup.controls).forEach(field => {
    const control = formGroup.get(field);
    if (control instanceof FormControl) {
      control.markAsTouched({ onlySelf: true });
    } else if (control instanceof FormGroup) {
      this.validateAllFormFields(control);
    }
  });
}

export function devRestoreFormState<K extends keyof FormState>(formValue: any, form: FormGroup) {
  if (environment.hmr === false || formValue == null) { return; }
  console.log(formValue);

  Object.keys(form.controls).forEach(field => {
    const control = form.get(field);
    control.setErrors( formValue.controls[field].errors);

    if (formValue.controls[field].disabled) {
      control.disable({onlySelf: true, emitEvent: false});
    } else {
      control.enable({onlySelf: true, emitEvent: false});
    }

    if (formValue.controls[field].touched) {
      control.markAsTouched({ onlySelf: true });
    } else {
      control.markAsUntouched({ onlySelf: true });
    }

    if (formValue.controls[field].dirty) {
      control.markAsDirty({ onlySelf: true });
    } else {
      control.markAsPristine({ onlySelf: true });
    }

    if (control instanceof FormControl) {
      (control as FormControl).setValue(formValue.controls[field].value, {emitEvent: false, emitModelToViewChange: true});
    } else if (control instanceof FormGroup) {
      this.devRestoreFormState(formValue, control);
    }
  });
}
