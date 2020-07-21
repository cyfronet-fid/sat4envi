import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {SearchResultsComponent} from './search-results.component';
import {MapModule} from '../../map.module';
import {take} from 'rxjs/operators';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RemoteConfigurationTestingProvider} from '../../../../app.configuration.spec';
import {SentinelSearchFactory} from '../../state/sentinel-search/sentinel-search.factory.spec';
import {SentinelSearchResult} from '../../state/sentinel-search/sentinel-search.model';

describe('SearchResultsComponent', () => {
  let component: SearchResultsComponent;
  let fixture: ComponentFixture<SearchResultsComponent>;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, RouterTestingModule, HttpClientTestingModule],
      providers: [RemoteConfigurationTestingProvider]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    de = fixture.debugElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display loading', () => {
    component.isLoading = true;
    fixture.detectChanges();
    expect(de.queryAll(By.css('.loading.loading--results')).length).not.toBe(0)
  });

  describe('results', () => {
    let result: SentinelSearchResult
    beforeEach(() => {
      result = SentinelSearchFactory.buildSearchResult()
      component.searchResults = [result];
      fixture.detectChanges();
    });

    it('should display results', () => {
      expect(de.queryAll(By.css('[data-test="search-result-entry"]')).length).toBe(1);
    });

    it('should emit showDetails when details button is clicked', async () => {
      const showDetailsEmitted = component.showDetails.pipe(take(1)).toPromise();
      de.query(By.css('[data-test="search-result-entry"]:first-child [data-test="show-details-button"]')).nativeElement.click();
      expect(await showDetailsEmitted).toEqual(result)
    });

    it('download link should have result.url as href', () => {
      expect(de.query(By.css('[data-test="search-result-entry"]:first-child a.download-link.download')).attributes.href)
        .toBe(result.url);
    });
  });

  it('should display no results', () => {
    component.searchResults = [];
    fixture.detectChanges();
    expect(de.query(By.css('[data-test="no-results"]'))).toBeTruthy();
  });

  it('should emit close when clicking back button', async () => {
    const closeEmitted = component.close.pipe(take(1)).toPromise();
    de.query(By.css('[data-test="back-button"]')).nativeElement.click();
    expect(await closeEmitted).toBeUndefined()
  });

  it('should emit close when clicking close button', async () => {
    const closeEmitted = component.close.pipe(take(1)).toPromise();
    de.query(By.css('[data-test="close-button"]')).nativeElement.click();
    expect(await closeEmitted).toBeUndefined()
  });

  describe('error', () => {
    beforeEach(() => {
      component.error = {__general__: ['some error']};
      fixture.detectChanges()
    });

    it('should emit reload when clicking close button', async () => {
      const reloadEmitted = component.reload.pipe(take(1)).toPromise();
      de.query(By.css('[data-test="reload"]')).nativeElement.click();
      expect(await reloadEmitted).toBeUndefined()
    });

    it('should display error', () => {
      expect(de.query(By.css('[data-test="error-container"]'))).toBeTruthy();
    });
  })
});
