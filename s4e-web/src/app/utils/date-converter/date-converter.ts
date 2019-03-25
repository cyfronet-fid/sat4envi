import {JsonConverter, JsonCustomConvert} from 'json2typescript';
import {format, parse} from 'date-fns';
import {IConstants, S4E_CONSTANTS} from '../../app.constants';
import {InjectorModule} from '../../injector.module';

@JsonConverter
export class DateConverter implements JsonCustomConvert<Date> {
  private _constants: IConstants|undefined = undefined;

  get CONSTANTS() {
    if (this._constants) {
      return this._constants;
    }
    return this._constants = InjectorModule.Injector.get<IConstants>(S4E_CONSTANTS);
  }

  serialize(date: Date): any {
    if (date == null) {
      return null;
    }
    return format(date, this.CONSTANTS.backendDateFormat);
  }
  deserialize(date: string|null): Date {
    if (date == null) {
      return null;
    }
    return parse(date, this.CONSTANTS.backendDateFormat, new Date());
  }
}
