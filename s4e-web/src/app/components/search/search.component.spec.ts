import { SearchComponent } from './search.component';
import { SearchModule } from './search.module';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Component, DebugElement, Directive } from '@angular/core';
import { By } from '@angular/platform-browser';

@Component({
  selector: 's4e-tile-mock-component',
  template: `
    <s4e-search
      placeholder="Wpisz szukaną ... ..."
      [searchResults]="searchResults"
      [isSearchLoading]="isSearchLoading"
      [isSearchOpen]="isSearchOpen"
      [value]="searchValue"

      (selectResult)="selectResult($event)"
      (refreshResults)="refreshResults($event)"
      (resetSearch)="resetSearch()"
      (selectFirstResult)="selectFirstResult()"
    >
      <ng-template #result let-result>
        <span class="name">{{ result.name }}</span>
      </ng-template>
    </s4e-search>
  `
})
export class SearchMockComponent {
  searchResults = [
    {
      name: 'example#1'
    },
    {
      name: 'example#2'
    }
  ];
  isSearchLoading = false;
  isSearchOpen = false;
  searchValue = '';

  selectResult(result: any) {}
  refreshResults(value: string) {}
  resetSearch() {}
  selectFirstResult() {}
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
        SearchModule
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
    const refreshResultsSpy = spyOn(component, 'refreshResults');
    const valueToSearch = 'example';
    sendInput(valueToSearch)
      .then(() => {
        const results = de.queryAll(By.css('.name'));
        expect(results.length).toEqual(2);
      });
  });

  it('should emit reset on reset button click', () => {
    component.searchValue = 'example';

    const resetSpy = spyOn(component, 'resetSearch');
    const resetBtn = searchDe.query(By.css('.reset_search_button')).nativeElement;
    resetBtn.click();

    fixture.detectChanges();

    expect(resetSpy).toHaveBeenCalled();
  });

  it('should select first on search button click', () => {
    const selectFirstResultSpy = spyOn(component, 'selectFirstResult');

    component.searchValue = 'example';
    component.isSearchOpen = true;

    fixture.detectChanges();

    const selectFirstResultBtn = searchDe.query(By.css('.search__button')).nativeElement;
    selectFirstResultBtn.click();

    fixture.detectChanges();

    expect(selectFirstResultSpy).toHaveBeenCalled();
  });

  it('should emit result on select result click', () => {
    const refreshResultsSpy = spyOn(component, 'refreshResults');
    const selectResultSpy = spyOn(component, 'selectResult');
    const valueToSearch = 'example';
    sendInput(valueToSearch)
      .then(() => {
        const results = de.queryAll(By.css('.name'));
        expect(results.length).toEqual(2);

        const firstResult = de.queryAll(By.css('.name'))[0].nativeElement;
        firstResult.click();

        fixture.detectChanges();

        expect(selectResultSpy).toHaveBeenCalledWith(component.searchResults.shift());
      });
  });

  function sendInput(text: string) {
    searchInput.value = text;
    searchInput.dispatchEvent(new Event('input'));
    component.isSearchOpen = true;
    fixture.detectChanges();
    return fixture.whenStable();
  }
});
