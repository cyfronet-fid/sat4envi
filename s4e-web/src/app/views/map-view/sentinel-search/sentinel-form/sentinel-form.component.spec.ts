import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {SentinelFormComponent} from './sentinel-form.component';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {SentinelFloatParamFactory, SentinelSearchMetadataFactory} from '../../state/sentinel-search/sentinel-search.factory.spec';
import {FormControl} from '@angular/forms';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';

describe('SentinelFormComponent', () => {
  let component: SentinelFormComponent;
  let fixture: ComponentFixture<SentinelFormComponent>;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule],
      providers: [TestingConfigProvider]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SentinelFormComponent);
    de = fixture.debugElement;
    component = fixture.componentInstance;
    component.paramsDef = SentinelSearchMetadataFactory.build().sections[0].params;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set form on setting paramsDef', () => {
    expect(Object.entries(component.form.controls).length).toBe(4);
    expect(component.form.get('satellitePlatform')).toBeInstanceOf(FormControl);
    expect(component.form.get('productType')).toBeInstanceOf(FormControl);
    expect(component.form.get('cloudCover')).toBeInstanceOf(FormControl);
    expect(component.form.get('relativeOrbitNumber')).toBeInstanceOf(FormControl);

    const spy = spyOn((component as any).valueChangeSub, 'unsubscribe');
    spyOn(component, 'onChange');

    component.paramsDef = [SentinelFloatParamFactory.build({queryParam: 'testParam'} as any)];
    expect(spy).toHaveBeenCalled();
    expect(component.onChange).toHaveBeenCalled();

    expect(Object.entries(component.form.controls).length).toBe(1);
    expect(component.form.get('testParam')).toBeInstanceOf(FormControl);
  });

  it('floatTooltip should have message for float with min and max', () => {
    expect(
      component.floatTooltip(SentinelFloatParamFactory.build({min: 0, max: 5} as any))
    ).toBe('od 0 do 5');
  });

  it('should work as formControl and strip nulls from the value passed', () => {
    const onChange = jest.fn();

    component.registerOnChange(onChange);
    expect(onChange).toHaveBeenCalledWith({productType: 'GRDM', satellitePlatform: 'Sentinel-1A'});
    component.form.get('productType').setValue('SLC_');
    component.form.get('cloudCover').setValue(10);
    expect(onChange).toHaveBeenCalledWith({productType: 'SLC_', satellitePlatform: 'Sentinel-1A', cloudCover: 10});
  });

  it('should disable form', () => {
    component.setDisabledState(true);
    expect(component.form.disabled).toBeTruthy();
    component.setDisabledState(false);
    expect(component.form.disabled).toBeFalsy();
  });

  it('should writeValues', () => {
    component.writeValue({productType: 'SLC_'});
    expect(component.form.value).toEqual({
      productType: 'SLC_',
      satellitePlatform: 'Sentinel-1A',
      relativeOrbitNumber: null,
      cloudCover: null
    });
  });

  it('should retain disable state when changing paramsDef', () => {
    component.setDisabledState(true);
    component.paramsDef = [SentinelFloatParamFactory.build({queryParam: 'testParam'} as any)];
    expect(component.form.disabled).toBeTruthy();
  });

  it('should generate controls in the template', () => {
    expect(de.query(By.css('#satellitePlatform'))).toBeTruthy();
    expect(de.query(By.css('#productType'))).toBeTruthy();
    expect(de.query(By.css('#cloudCover'))).toBeTruthy();
    expect(de.query(By.css('#relativeOrbitNumber'))).toBeTruthy();
  });
});
