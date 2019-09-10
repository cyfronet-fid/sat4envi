import {JsonConverter, JsonCustomConvert} from 'json2typescript';
import {format, parse} from 'date-fns';
import {InjectorModule} from '../../common/injector.module';
import {IConfiguration} from '../../app.configuration';
import {S4E_CONFIG} from '../initializer/config.service';

@JsonConverter
export class DateConverter implements JsonCustomConvert<Date> {
  private _config: IConfiguration | undefined = undefined;

  get CONFIG() {
    if (this._config) {
      return this._config;
    }
    return this._config = InjectorModule.Injector.get<IConfiguration>(S4E_CONFIG);
  }

  serialize(date: Date): any {
    if (date == null) {
      return null;
    }
    return format(date, this.CONFIG.backendDateFormat);
  }

  deserialize(date: string | null): Date {
    if (date == null) {
      return null;
    }
    return parse(date, this.CONFIG.backendDateFormat, new Date());
  }
}
