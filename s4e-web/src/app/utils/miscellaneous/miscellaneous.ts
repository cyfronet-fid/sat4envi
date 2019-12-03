import {FormControl, FormGroup} from '@ng-stack/forms';
import {FormState} from '../../state/form/form.model';
import {environment} from '../../../environments/environment';
import {HashMap} from '@datorama/akita';

export type ServerApiError = HashMap<string[]> | { __exception__: string, __stacktrace__: string, __general__: string[] }

export function disableEnableForm(disable: boolean, form: FormGroup<any> | FormControl<any>) {
  if (disable) {
    form.disable();
  } else {
    form.enable();
  }
}

export function validateAllFormFields(formGroup: FormGroup<any>) {
  Object.keys(formGroup.controls).forEach(field => {
    const control = formGroup.get(field);
    if (control instanceof FormControl) {
      control.markAsTouched({onlySelf: true});
    } else if (control instanceof FormGroup) {
      this.validateAllFormFields(control);
    }
  });
}

export function devRestoreFormState<K extends keyof FormState>(formValue: any, form: FormGroup) {
  if (environment.hmr === false || formValue == null) {
    return;
  }
  console.log(formValue);

  Object.keys(form.controls).forEach(field => {
    const control = form.get(field);
    control.setErrors(formValue.controls[field].errors);

    if (formValue.controls[field].disabled) {
      control.disable({onlySelf: true, emitEvent: false});
    } else {
      control.enable({onlySelf: true, emitEvent: false});
    }

    if (formValue.controls[field].touched) {
      control.markAsTouched({onlySelf: true});
    } else {
      control.markAsUntouched({onlySelf: true});
    }

    if (formValue.controls[field].dirty) {
      control.markAsDirty({onlySelf: true});
    } else {
      control.markAsPristine({onlySelf: true});
    }

    if (control instanceof FormControl) {
      (control as FormControl).setValue(formValue.controls[field].value, {emitEvent: false, emitModelToViewChange: true});
    } else if (control instanceof FormGroup) {
      this.devRestoreFormState(formValue, control);
    }
  });
}
