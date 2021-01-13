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

import { TilesDashboardModule } from './tiles-dashboard.module';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement, Component } from '@angular/core';

@Component({
  selector: 's4e-tiles-dashboards-mock-component',
  template: `
    <s4e-tiles-dashboard>
      <h1 dashboard-title>Witamy w panelu administratora Sat4Envi</h1>
      <p dashboard-description>Jesteś superadministratorem</p>
      <ng-container dashboard-tiles>
        <p>Test</p>
      </ng-container>
    </s4e-tiles-dashboard>
  `
})
export class TilesDashboardMockComponent {}

describe('TilesDashboardComponent', () => {
  let component: TilesDashboardMockComponent;
  let fixture: ComponentFixture<TilesDashboardMockComponent>;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        TilesDashboardModule
      ],
      declarations: [
        TilesDashboardMockComponent
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TilesDashboardMockComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display', () => {
    const headerTitle = de.nativeElement.querySelector('.content__header h1');
    expect(headerTitle.innerHTML).toEqual('Witamy w panelu administratora Sat4Envi');

    const headerDescription = de.nativeElement.querySelector('.content__header p');
    expect(headerDescription.innerHTML).toEqual('Jesteś superadministratorem');

    const tilesItem = de.nativeElement.querySelector('.panel p');
    expect(tilesItem.innerHTML).toEqual('Test');
  });
});
