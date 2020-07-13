import { QueryEntity, EntityStore, StoreConfig } from '@datorama/akita';
import { RouterTestingModule } from '@angular/router/testing';
import { SearchComponent } from './search.component';
import { SearchModule } from './search.module';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Component, DebugElement, Directive } from '@angular/core';
import { By } from '@angular/platform-browser';
import { of } from 'rxjs';

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
      imports: [
        SearchModule,
        RouterTestingModule
      ],
      declarations: [
        SearchMockComponent
      ]
    })
    .compileComponents();
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
    sendInput(valueToSearch)
      .then(() => {
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
    sendInput(valueToSearch)
      .then(() => {
        const selectActiveResultBtn = searchDe.query(By.css('.search__button')).nativeElement;
        selectActiveResultBtn.click();
        fixture.detectChanges();

        expect(selectResultSpy).toHaveBeenCalledWith(SEARCH_RESULTS[0]);
      });
  });

  it('should emit result on select result click', () => {
    const selectResultSpy = spyOn(component, 'selectResult');
    const valueToSearch = 'example';
    sendInput(valueToSearch)
      .then(() => {
        const results = de.queryAll(By.css('.name'));
        expect(results.length).toEqual(2);

        const firstResult = de.queryAll(By.css('.name'))[0].nativeElement;
        firstResult.click();

        fixture.detectChanges();

        expect(selectResultSpy).toHaveBeenCalledWith(SEARCH_RESULTS[0]);
      });
  });

  it('should emit result on `enter` `key press`', () => {
    const selectResultSpy = spyOn(component, 'selectResult');
    const valueToSearch = 'example';
    sendInput(valueToSearch)
      .then(() => {
        const results = de.queryAll(By.css('.name'));
        expect(results.length).toEqual(2);

        const ENTER = 13;
        keyPress(ENTER);

        fixture.detectChanges();

        expect(selectResultSpy).toHaveBeenCalledWith(SEARCH_RESULTS[0]);
      });
  });

  it('should emit second result on `arrow down`', () => {
    const selectResultSpy = spyOn(component, 'selectResult');
    const valueToSearch = 'example';
    sendInput(valueToSearch)
      .then(() => {
        const results = de.queryAll(By.css('.name'));
        expect(results.length).toEqual(2);

        const ARROW_DOWN = 40;
        keyPress(ARROW_DOWN);

        fixture.detectChanges();

        const ENTER = 13;
        keyPress(ENTER);

        fixture.detectChanges();

        expect(selectResultSpy).toHaveBeenCalledWith(SEARCH_RESULTS[1]);
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

  function keyPress(key) {
    const event = document.createEvent('Event');
    event.keyCode = key;
    event.key = key;
    event.initEvent('keydown');
    document.dispatchEvent(event);
  }
});
