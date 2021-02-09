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

import {QueryEntity, EntityStore, StoreConfig} from '@datorama/akita';
import {RouterTestingModule} from '@angular/router/testing';
import {SearchComponent} from './search.component';
import {SearchModule} from './search.module';
import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {Component, DebugElement, Directive} from '@angular/core';
import {By} from '@angular/platform-browser';
import {of} from 'rxjs';
import {LocationSearchResultsStore} from '../../views/map-view/state/location-search-results/locations-search-results.store';

const SEARCH_RESULTS = [
  {
    name: 'example#1'
  },
  {
    name: 'example#2'
  }
];

@StoreConfig({name: 'Mock'})
class StoreMock extends EntityStore<any> {}

@Component({
  selector: 's4e-tile-mock-component',
  template: `
    <s4e-search
      placeholder="Wpisz szukaną ... ..."
      [query]="query"
      [store]="store"
      [value]="searchValue"
      (valueChange)="refreshResults($event)"
      (selectResult)="selectResult($event)"
    >
      <ng-template #result let-result>
        <span class="name">{{ result.name }}</span>
      </ng-template>
    </s4e-search>
  `
})
export class SearchMockComponent {
  searchValue = '';
  store = new StoreMock();
  query = new QueryEntity<any, any>(this.store);

  constructor() {
    this.store.set(SEARCH_RESULTS);
    this.store.setLoading(false);
  }

  selectResult(result: any) {}
  refreshResults(value: string) {}
}

describe('SearchComponent', () => {
  let component: SearchMockComponent;
  let fixture: ComponentFixture<SearchMockComponent>;
  let searchDe: DebugElement;
  let de: DebugElement;
  let searchInput: any;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SearchModule, RouterTestingModule],
      declarations: [SearchMockComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchMockComponent);
    component = fixture.componentInstance;
    searchDe = fixture.debugElement.query(By.directive(SearchComponent));
    de = fixture.debugElement;
    searchInput = searchDe.query(By.css('input')).nativeElement;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display search results', () => {
    const valueToSearch = 'example';
    sendInput(valueToSearch).then(() => {
      const results = de.queryAll(By.css('.name'));
      expect(results.length).toEqual(2);
    });
  });

  it('should emit empty string on reset button click', () => {
    component.searchValue = 'example';

    fixture.detectChanges();

    const refreshResultsSpy = spyOn(component, 'refreshResults');
    const resetBtn = searchDe.query(By.css('.reset_search_button')).nativeElement;
    resetBtn.click();

    fixture.detectChanges();

    expect(refreshResultsSpy).toHaveBeenCalledWith('');
  });

  it('should select active result on search button click', () => {
    const selectResultSpy = spyOn(component, 'selectResult');
    const valueToSearch = 'example';
    sendInput(valueToSearch).then(() => {
      const selectActiveResultBtn = searchDe.query(By.css('.search__button'))
        .nativeElement;
      selectActiveResultBtn.click();
      fixture.detectChanges();

      expect(selectResultSpy).toHaveBeenCalledWith(SEARCH_RESULTS[0]);
    });
  });

  it('should emit result on select result click', () => {
    const selectResultSpy = spyOn(component, 'selectResult');
    const valueToSearch = 'example';
    sendInput(valueToSearch).then(() => {
      const results = de.queryAll(By.css('.name'));
      expect(results.length).toEqual(2);

      const firstResult = de.queryAll(By.css('.name'))[0].nativeElement;
      firstResult.click();

      fixture.detectChanges();

      expect(selectResultSpy).toHaveBeenCalledWith(SEARCH_RESULTS[0]);
    });
  });

  function sendInput(text: string) {
    searchInput.click();
    fixture.detectChanges();

    searchInput.value = text;
    searchInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    return fixture.whenStable();
  }
});
