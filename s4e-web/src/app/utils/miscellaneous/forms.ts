import {FormGroup} from '@ng-stack/forms';
import {IConfiguration} from '../../app.configuration';
import {InjectorModule} from '../../common/injector.module';
import {S4eConfig} from '../initializer/config.service';
import {ServerApiError} from './miscellaneous';

export function connectErrorsToForm(errors: ServerApiError | null, form: FormGroup) {
  const CONFIG: IConfiguration = InjectorModule.Injector.get(S4eConfig);

  if (errors == null) {
    form.setErrors({});
    return;
  }

  Object.entries(errors).filter(([key, value]: [string, string[]]) => form.get(key) != null)
  // .map(([key, value]: [string, string[]]) => [key, value.reduce((pv, cv, i) => {pv[`server_${i}`] = cv; return pv}, {})])
    .forEach(([key, value]: [string, string[]]) => {
      if (key === CONFIG.generalErrorKey) {
        form.setErrors({[CONFIG.generalErrorKey]: value}, {emitEvent: false});
      } else {
        form.get(key).setErrors({server: value}, {emitEvent: true});
      }
    });
}
