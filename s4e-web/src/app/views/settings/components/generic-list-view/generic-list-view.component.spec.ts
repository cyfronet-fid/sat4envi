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
import {GenericListViewComponent} from './generic-list-view.component';
import {GenericListViewModule} from './generic-list-view.module';
import * as Factory from 'factory.ts';
import {take} from 'rxjs/operators';
import {Component, DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';

interface Item {
  id: number;
  name: string;
}

const CustomItemFactory = Factory.makeFactory<Item>({
  id: Factory.each(i => i),
  name: Factory.each(i => `Sample Item #${i % 10}`)
});

describe('GenericListViewComponent', () => {
  let component: GenericListViewComponent;
  let fixture: ComponentFixture<GenericListViewComponent>;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [GenericListViewModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GenericListViewComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    component.loading = false;
    component._items = CustomItemFactory.buildList(10);
  });

  describe('searchable', () => {
    beforeEach(() => {
      component.searchable = true;
      fixture.detectChanges();
    });

    it('should search offline', fakeAsync(() => {
      component.queryFc.setValue('#3');
      tick(250);
      expect(component.items.length).toBe(1);
    }));

    it('should search callback', async () => {
      component.offlineSearch = false;
      component.queryFc.setValue('#3');
      expect(await component.onquery.pipe(take(1)).toPromise()).toEqual('#3');
    });
  });

  describe('non searchable', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should show loading', () => {
      expect(de.query(By.css('[data-test="loading"]'))).toBeFalsy();
      component.loading = true;
      fixture.detectChanges();
      expect(de.query(By.css('[data-test="loading"]'))).toBeTruthy();
    });
  });
});

@Component({
  selector: 'mock-generic-list',
  template: `
    <s4e-generic-list-view [loading]="loading"
                           [items]="items"
                           [error]="error">
      <ng-container add-button>
        <button class="button button--secondary button--large" routerLink="../add-item"
                queryParamsHandling="preserve" i18n>Nowy Item</button>
      </ng-container>
      <ng-container description>
        [TODO] Tu możesz zarządzać itemami...
      </ng-container>
      <ng-container caption i18n>Zarządzanie Itemami</ng-container>
      <ng-container table-header>
        <th i18n>Nazwa</th>
        <th i18n>ID</th>
      </ng-container>
      <ng-template let-item="item">
        <td [attr.data-test-id]="item.id">{{item.name}}</td>
        <td>{{item.id}}</td>
      </ng-template>
    </s4e-generic-list-view>
  `,
  styles: []
})
class MockGenericListViewComponent {
  loading: boolean = false;
  items: Item[] = [];
  error: any|null = null;
}


describe('GenericListViewComponent MockComponent', () => {
  let component: MockGenericListViewComponent;
  let fixture: ComponentFixture<MockGenericListViewComponent>;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MockGenericListViewComponent],
      imports: [GenericListViewModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MockGenericListViewComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    component.items = CustomItemFactory.buildList(10);
    fixture.detectChanges();
  });

  it('should show items', () => {
    expect(de.queryAll(By.css('[data-test-id]')).length).toBe(10);
  });
});
