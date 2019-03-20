import {deserializeJsonResponse, disableEnableForm} from './miscellaneous';
import {FormControl, FormGroup} from '@angular/forms';
import {JsonObject, JsonProperty} from 'json2typescript';
import {DateConverter} from '../date-converter/date-converter';
import {async, TestBed} from '@angular/core/testing';
import {InjectorModule} from '../../injector.module';
import {TestingConstantsProvider} from '../../app.constants.spec';
import {format} from 'date-fns';
import {Injector} from '@angular/core';

describe('disableEnableForm', () => {
  let form: FormGroup;

  beforeEach(() => {
    form = new FormGroup({
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
      providers: [TestingConstantsProvider, Injector],
    });
    TestBed.get(InjectorModule);
  }));

  it('should work', function () {
    const time = new Date('2019-03-21T23:04:19.000Z');

    expect(deserializeJsonResponse({
      _login: 'abc',
      _time: format(time, 'yyyy-MM-dd\'T\'HH:mm:ss')
    }, Data)).toEqual({
      login: 'abc',
      time: time
    });
  });
});
