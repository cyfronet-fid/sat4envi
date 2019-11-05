import {DateConverter} from './date-converter';
import {TestBed} from '@angular/core/testing';
import {InjectorModule} from '../../common/injector.module';
import {Injector} from '@angular/core';
import {TestingConfigProvider} from '../../app.configuration.spec';

describe('DateConverter', () => {
  let service: DateConverter;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [InjectorModule],
      providers: [TestingConfigProvider, Injector],
    });
    TestBed.get(InjectorModule);

    service = new DateConverter();
  });

  it('should return null if null given to serialize', () => {
    expect(service.serialize(null)).toEqual(null);
  });

  it('should return null if null given to deserialize', () => {
    expect(service.deserialize(null)).toEqual(null);
  });

  it('should serialize', () => {
    expect(service.serialize(new Date('2019-03-21T22:04:19Z')))
      .toEqual('2019-03-21T22:04:19Z');
  });

  it('should deserialize', () => {
    expect(service.deserialize('2019-03-21T23:04:19Z'))
      .toEqual(new Date(Date.UTC(2019, 2, 21, 23, 4, 19, 0)));
  });
});
