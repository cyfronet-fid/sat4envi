import {disableEnableForm, resizeImage} from './miscellaneous';
import {async, TestBed} from '@angular/core/testing';
import {InjectorModule} from '../../common/injector.module';
import {Injector} from '@angular/core';
import {TestingConfigProvider} from '../../app.configuration.spec';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {DOCUMENT} from '@angular/common';

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

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [InjectorModule],
      providers: [TestingConfigProvider, Injector],
    });
    TestBed.get(InjectorModule);
  }));
});
