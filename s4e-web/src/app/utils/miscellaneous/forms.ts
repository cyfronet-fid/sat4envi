import {FormGroup} from '@ng-stack/forms';
import {ServerApiError} from './miscellaneous';
import environment from 'src/environments/environment';

export function connectErrorsToForm(errors: ServerApiError | null, form: FormGroup) {
  if (errors == null) {
    form.setErrors({});
    return;
  }

  Object.entries(errors).filter(([key, value]: [string, string[]]) => form.get(key) != null)
  // .map(([key, value]: [string, string[]]) => [key, value.reduce((pv, cv, i) => {pv[`server_${i}`] = cv; return pv}, {})])
    .forEach(([key, value]: [string, string[]]) => {
      if (key === environment.generalErrorKey) {
        form.setErrors({[environment.generalErrorKey]: value}, {emitEvent: false});
      } else {
        form.get(key).setErrors({server: value}, {emitEvent: true});
      }
    });
}
