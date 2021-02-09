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

import {By} from '@angular/platform-browser';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {ItemsPickerComponent} from './layer-picker.component';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';

fdescribe('ItemsPickerComponent', () => {
  let component: ItemsPickerComponent;
  let fixture: ComponentFixture<ItemsPickerComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ItemsPickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have star on enabled', () => {
    component.hasFavourite = true;
    component.items = [
      {
        cid: 0,
        label: '',
        active: true,
        favourite: false,
        isLoading: false,
        isFavouriteLoading: false
      }
    ];
    spyOn(component.isFavouriteSelected, 'emit');
    fixture.detectChanges();

    expect(fixture.debugElement.queryAll(By.css('.fa-star'))).toBeTruthy();

    fixture.debugElement.queryAll(By.css('.fa-star'))[0].nativeElement.click();
    fixture.detectChanges();

    expect(component.isFavouriteSelected.emit).toHaveBeenCalledWith({
      ID: 0,
      isFavourite: true
    });
  });

  it('should not have star on disabled', () => {
    component.hasFavourite = false;
    component.items = [
      {
        cid: 0,
        active: true,
        favourite: false,
        category: undefined,
        isFavouriteLoading: false,
        isLoading: false,
        label: ''
      }
    ];
    fixture.detectChanges();

    expect(fixture.debugElement.queryAll(By.css('.fa-star')).length > 0).toBeFalsy();
  });
});
