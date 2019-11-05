import {JsonConverter, JsonCustomConvert} from 'json2typescript';
import {InjectorModule} from '../../common/injector.module';
import {IConfiguration} from '../../app.configuration';
import {S4eConfig} from '../initializer/config.service';
import moment from 'moment';

@JsonConverter
export class DateConverter implements JsonCustomConvert<Date> {
  private _DATE_FORMAT_DES: string;
  private _DATE_FORMAT_SER: string;

  get DATE_FORMAT_DES() {
    if (this._DATE_FORMAT_DES) {
      return this._DATE_FORMAT_DES;
    }
    const config = InjectorModule.Injector.get<IConfiguration>(S4eConfig);
    return this._DATE_FORMAT_DES = config.backendDateFormat
  }

  get DATE_FORMAT_SER() {
    if (this._DATE_FORMAT_SER) {
      return this._DATE_FORMAT_SER;
    }
    const config = InjectorModule.Injector.get<IConfiguration>(S4eConfig);
    return this._DATE_FORMAT_SER = config.momentDateFormat;
  }

  serialize(date: Date): any {
    if (date == null) {
      return null;
    }

    return moment.utc(date).format(this.DATE_FORMAT_SER);
  }

  deserialize(date: string | null): Date {
    if (date == null) {
      return null;
    }

    return moment.utc(date, this.DATE_FORMAT_DES).toDate();
  }
}
