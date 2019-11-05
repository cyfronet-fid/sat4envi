import {deserializeJsonResponse, disableEnableForm} from './miscellaneous';
import {JsonObject, JsonProperty} from 'json2typescript';
import {DateConverter} from '../date-converter/date-converter';
import {async, TestBed} from '@angular/core/testing';
import {InjectorModule} from '../../common/injector.module';
import {Injector} from '@angular/core';
import {TestingConfigProvider} from '../../app.configuration.spec';
import {FormControl, FormGroup} from '@ng-stack/forms';

export interface FS {
  login: string;
}

describe('disableEnableForm', () => {
  let form: FormGroup<FS>;

  beforeEach(() => {
    form = new FormGroup<FS>({
      login: new FormControl('')
    });
  });

  it('should disable', () => {
    disableEnableForm(true, form);
    expect(form.disabled).toBeTruthy();
  });

  it('should enable', () => {
    disableEnableForm(false, form);
    expect(form.disabled).toBeFalsy();
  });
});

describe('deserializeJsonResponse', function () {
  interface IData {
    time: Date;
    login: string;
  }

  @JsonObject
  class Data implements IData {
    @JsonProperty('_login', String)
    login: string = undefined;
    @JsonProperty('_time', DateConverter)
    time: Date = undefined;
  }

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [InjectorModule],
      providers: [TestingConfigProvider, Injector],
    });
    TestBed.get(InjectorModule);
  }));

  it('should work', function () {
    const time = new Date('2019-03-21T23:04:19.000Z');

    expect(deserializeJsonResponse({
      _login: 'abc',
      _time: '2019-03-21T23:04:19Z'
    }, Data)).toEqual({
      login: 'abc',
      time: time
    });
  });
});
