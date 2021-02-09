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

import {TilesDashboardModule} from './../tiles-dashboard.module';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {Component, DebugElement} from '@angular/core';

@Component({
  selector: 's4e-tile-mock-component',
  template: `
    <s4e-tile>
      <header class="panel__header" tile-title>Zarządzaj instytucjami</header>
      <p i18n tile-description>
        Lista wszystkich instytucji. W tym miejscu możesz je edytować
      </p>
      <button footer-navigation>Zarządzaj</button>
    </s4e-tile>
  `
})
export class TileMockComponent {}

describe('TileComponent', () => {
  let component: TileMockComponent;
  let fixture: ComponentFixture<TileMockComponent>;
  let de: DebugElement;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [TilesDashboardModule],
        declarations: [TileMockComponent]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(TileMockComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display', () => {
    const tileTitle = de.nativeElement.querySelector('.panel__item .panel__header');
    expect(tileTitle.innerHTML).toEqual('Zarządzaj instytucjami');

    const tileDescription = de.nativeElement.querySelector('.panel__item p');
    expect(tileDescription.innerHTML).toEqual(
      ' Lista wszystkich instytucji. W tym miejscu możesz je edytować '
    );

    const navigationBtn = de.nativeElement.querySelector('.panel__footer button');
    expect(navigationBtn.textContent).toEqual('Zarządzaj');
  });
});
