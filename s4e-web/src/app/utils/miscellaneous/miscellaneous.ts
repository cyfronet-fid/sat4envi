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

import {FormControl, FormGroup} from '@ng-stack/forms';
import {FormControl as AngularFormControl, FormGroup as AngularFormGroup} from '@angular/forms';
import {FormState} from '../../state/form/form.model';
import {environment} from '../../../environments/environment';
import {HashMap} from '@datorama/akita';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {Base64Image} from '../../common/types';
import {Observable} from 'rxjs';
import {InjectorModule} from '../../common/injector.module';
import {map, take} from 'rxjs/operators';
import {DOCUMENT} from '@angular/common';

export type ServerApiError = HashMap<string[]> | { __exception__: string, __stacktrace__: string, __general__: string[] }

export function disableEnableForm(disable: boolean, form: FormGroup<any> | FormControl<any> | AngularFormGroup | AngularFormControl) {
  if (disable) {
    form.disable();
  } else {
    form.enable();
  }
}

export function validateAllFormFields(formGroup: FormGroup<any>, updateFormManager?: { formKey: keyof FormState, fm: AkitaNgFormsManager<FormState> }) {
  Object.keys(formGroup.controls)
    .filter(field => !!formGroup.get(field))
    .forEach(field => {
      const control = formGroup.get(field);
      if (control instanceof FormControl) {
        control.markAsTouched({onlySelf: true});
      } else if (control instanceof FormGroup) {
        this.validateAllFormFields(control);
      }
    });

  if (updateFormManager != null && environment.hmr) {
    (updateFormManager.fm as any).updateStore(updateFormManager.formKey, formGroup);
  }
}

export function devRestoreFormState<K extends keyof FormState>(formValue: any, form: FormGroup) {
  if (environment.hmr === false || formValue == null) {
    return;
  }

  Object.keys(form.controls)
    .filter(field => {
      if (!formValue.controls[field]) {
        console.error(`Control for field ${field} can't be found`)
      }

      return !!formValue.controls[field];
    })
    .forEach(field => {
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


export function resizeImage(data: Base64Image, outWidth: number, outHeight: number): Observable<Base64Image> {
  const document = InjectorModule.Injector.get<Document>(DOCUMENT);
  let canvas: HTMLCanvasElement = document.createElement('canvas') as HTMLCanvasElement;
  canvas.width = outWidth;
  canvas.height = outHeight;
  const ctx = canvas.getContext('2d');

  const out: Observable<any> = new Observable(
    (observer) => {
      const image = new Image();
      image.onload = () => {
        observer.next(image);
        observer.complete();
      };

      image.src = data;
    });

  return out.pipe(
    take(1),
    map((img) => {
      let w: number;
      let h: number;
      let x: number = 0;
      let y: number = 0;

      const ratio = outWidth / outHeight;
      const imgRatio = img.width / img.height;

      // img is wider
      if (ratio  < imgRatio) {
        h = outHeight;
        w = imgRatio * h;
        x = -(w * 0.5 - outWidth * 0.5);
      }
      //img is higher
      else {
        w = outWidth;
        h = w / imgRatio;
        y = -(h * 0.5 - outHeight * 0.5);
      }

      ctx.drawImage(img, x, y, w, h);
      return canvas.toDataURL('image/png');
    }));
}

export function toHashMap<T>(list: T[], key: keyof T): HashMap<T> {
  return list.reduce((prev: HashMap<T>, curr: T) => {
    prev[String(curr[key])] = curr;
    return prev;
  }, {});
}

export function isEmptyObject(obj: object): boolean {
  return Object.keys(obj).length === 0 && obj.constructor === Object;
}

export function stripEmpty(obj: object): object {
  const r = {};

  Object.entries(obj).filter(([k, v]) => v != null && v != '').forEach(([k, v]) => {
    r[k] = v
  });

  return r;
}
