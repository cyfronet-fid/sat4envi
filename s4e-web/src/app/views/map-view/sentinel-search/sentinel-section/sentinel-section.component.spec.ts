/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import { SentinelSectionComponent } from './sentinel-section.component';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {SentinelSearchMetadataFactory, SentinelSectionFactory} from '../../state/sentinel-search/sentinel-search.factory.spec';
import {FormControl} from '@angular/forms';
import {SentinelSearchService} from '../../state/sentinel-search/sentinel-search.service';
import {SentinelSearchQuery} from '../../state/sentinel-search/sentinel-search.query';
import {ReplaySubject, Subject} from 'rxjs';
import {take} from 'rxjs/operators';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';


describe('SentinelSectionComponent', () => {
  let component: SentinelSectionComponent;
  let fixture: ComponentFixture<SentinelSectionComponent>;
  let de: DebugElement;
  let service: SentinelSearchService;
  let query: SentinelSearchQuery;
  let visibleSentinels$: Subject<string[]>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    })
    .compileComponents();

    service = TestBed.get(SentinelSearchService);
    query = TestBed.get(SentinelSearchQuery);
    visibleSentinels$ = new ReplaySubject(1);

    spyOn(query, 'selectVisibleSentinels').and.returnValue(visibleSentinels$);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SentinelSectionComponent);
    de = fixture.debugElement;
    component = fixture.componentInstance;
    component.sentinel = SentinelSearchMetadataFactory.build().sections[0];
    visibleSentinels$.next([component.sentinel.name]);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('toggleVisibility should not call service if component is disabled', () => {
    const spy = spyOn(service, 'toggleSentinelVisibility');
    component.setDisabledState(true);
    component.toggleVisibility();
    expect(spy).not.toHaveBeenCalled();
  });

  it('toggleVisibility should call service', () => {
    const spy = spyOn(service, 'toggleSentinelVisibility');
    component.toggleVisibility();
    expect(spy).toHaveBeenCalled();
  });

  it('visible$ should depend on query', async () => {
    visibleSentinels$.next([]);
    expect(await component.visible$.pipe(take(1)).toPromise()).toBeFalsy();
    visibleSentinels$.next([component.sentinel.name]);
    expect(await component.visible$.pipe(take(1)).toPromise()).toBeTruthy();
  });

  it('disable form should work', () => {
    expect(component.form.disabled).toBeFalsy();
    expect(component.selectedFc.disabled).toBeFalsy();
    component.setDisabledState(true);
    expect(component.form.disabled).toBeTruthy();
    expect(component.selectedFc.disabled).toBeTruthy();
  });

  it('validate should return error only if section is selected', () => {
    const error = {__general__: ['error']};
    component.form.setErrors(error);
    expect(component.validate(new FormControl())).toEqual(error);
    component.selectedFc.setValue(false);
    expect(component.validate(new FormControl())).toEqual(null);
  });

  it('should behave as FormControl', () => {
    const onChange = jest.fn();

    component.registerOnChange(onChange);
    expect(onChange).toHaveBeenCalledWith({productType: 'GRDM', satellitePlatform: 'Sentinel-1A'});
    component.form.setValue({productType: '_SLC', satellitePlatform: 'Sentinel-1A', cloudCover: 10});
    expect(onChange).toHaveBeenCalledWith({productType: '_SLC', satellitePlatform: 'Sentinel-1A', cloudCover: 10});
  });

  it('should propagate {} if it is not selected', () => {
    const onChange = jest.fn();
    visibleSentinels$.next([]);
    component.registerOnChange(onChange);
    expect(onChange).toHaveBeenCalledWith({});
    visibleSentinels$.next([component.sentinel.name]);
    expect(onChange).toHaveBeenCalledWith({productType: 'GRDM', satellitePlatform: 'Sentinel-1A'});
  });

  it('setting new sentinel should reevaluate selectedFc value  based on the new sentinel name', () => {
    component.sentinel = SentinelSectionFactory.build({name: 'test-sentinel-1'});
    expect(component.selectedFc.value).toBeFalsy();
  });

  it('clicking header should toggleVisibility', () => {
    const spy = spyOn(component, 'toggleVisibility').and.stub();
    de.query(By.css('[data-test="sentinel-section-header"]')).nativeElement.click();
    expect(spy).toHaveBeenCalled();
  });

  it('content should be visible depending on the visible$', () => {
    expect(de.query(By.css('[data-test="sentinel-section-content"]:not([hidden=""])'))).toBeTruthy();
    visibleSentinels$.next([]);
    fixture.detectChanges();
    expect(de.query(By.css('[data-test="sentinel-section-content"][hidden=""]'))).toBeTruthy();
  });

  it('should callOnChange when visibility or value changes', fakeAsync(() => {
    const spy = jest.fn();
    component.registerOnChange(spy);
    component.form.setValue({productType: '', satellitePlatform: 'Sentinel-1A'})
    expect(spy).toHaveBeenNthCalledWith(4, {satellitePlatform: 'Sentinel-1A'});
    visibleSentinels$.next([]);
    expect(spy).toHaveBeenNthCalledWith(6, {});
    visibleSentinels$.next([component.sentinel.name]);
    expect(spy).toHaveBeenNthCalledWith(7, {satellitePlatform: 'Sentinel-1A'});
  }));
});
