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

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {SearchResultsComponent} from './search-results.component';
import {MapModule} from '../../map.module';
import {take, toArray} from 'rxjs/operators';
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

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [MapModule, RouterTestingModule, HttpClientTestingModule],
        providers: [RemoteConfigurationTestingProvider]
      }).compileComponents();
    })
  );

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
    expect(de.queryAll(By.css('.loading.loading--results')).length).not.toBe(0);
  });

  describe('results', () => {
    let result: SentinelSearchResult;
    beforeEach(() => {
      result = SentinelSearchFactory.buildSearchResult();
      component.searchResults = [result];
      fixture.detectChanges();
    });

    it('should display results', () => {
      expect(de.queryAll(By.css('[data-test="search-result-entry"]')).length).toBe(
        1
      );
    });

    it('should have thumbnail', () => {
      expect(
        de
          .query(By.css('.search-result__quicklook'))
          .nativeElement.getAttribute('style')
      ).toBe(`background: url(api/v1/scenes/${result.id}/download/thumbnail);`);
    });

    it('should have empty thumbnail', () => {
      result.image = null;
      component.searchResults = [result];
      fixture.detectChanges();
      expect(
        de
          .query(By.css('.search-result__quicklook'))
          .nativeElement.getAttribute('styles')
      ).toBeFalsy();
    });

    it('should emit showDetails when details button is clicked', async () => {
      const showDetailsEmitted = component.showDetails.pipe(take(1)).toPromise();
      de.query(
        By.css(
          '[data-test="search-result-entry"]:first-child [data-test="show-details-button"]'
        )
      ).nativeElement.click();
      expect(await showDetailsEmitted).toEqual(result);
    });

    it('download link should have result.url as href', () => {
      expect(
        de.query(
          By.css(
            '[data-test="search-result-entry"]:first-child a.download-link.download'
          )
        ).attributes.href
      ).toBe(result.url);
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
    expect(await closeEmitted).toBeUndefined();
  });

  it('should emit close when clicking close button', async () => {
    const closeEmitted = component.close.pipe(take(1)).toPromise();
    de.query(By.css('[data-test="close-button"]')).nativeElement.click();
    expect(await closeEmitted).toBeUndefined();
  });

  describe('error', () => {
    beforeEach(() => {
      component.error = {__general__: ['some error']};
      fixture.detectChanges();
    });

    it('should emit reload when clicking close button', async () => {
      const reloadEmitted = component.reload.pipe(take(1)).toPromise();
      de.query(By.css('[data-test="reload"]')).nativeElement.click();
      expect(await reloadEmitted).toBeUndefined();
    });

    it('should display error', () => {
      expect(de.query(By.css('[data-test="error-container"]'))).toBeTruthy();
    });
  });

  describe('pagination', () => {
    let results: SentinelSearchResult[];

    beforeEach(() => {
      results = SentinelSearchFactory.buildListSearchResult(100);
      component.searchResults = results.slice(0, 25);
      component.totalCount = results.length;
      component.currentPage = 0;
      component.resultPagesCount = 4;
      fixture.detectChanges();
    });

    it('should work', () => {
      expect(de.query(By.css('.pagination'))).not.toBeUndefined();
    });

    it('should have 4 pages + prev & next', () => {
      expect(de.queryAll(By.css('.pagination > li')).length).toBe(6);
    });

    it('clicking on page should emit changePage', async () => {
      const changePage$ = component.changePage
        .asObservable()
        .pipe(take(4), toArray())
        .toPromise();
      de.queryAll(By.css('.pagination > li'))[1]
        .query(By.css('a'))
        .nativeElement.click();
      de.queryAll(By.css('.pagination > li'))[2]
        .query(By.css('a'))
        .nativeElement.click();
      de.queryAll(By.css('.pagination > li'))[3]
        .query(By.css('a'))
        .nativeElement.click();
      de.queryAll(By.css('.pagination > li'))[4]
        .query(By.css('a'))
        .nativeElement.click();
      expect(await changePage$).toEqual([0, 1, 2, 3]);
    });

    it('prev should be disabled on first page', async () => {
      const changePage$ = component.changePage
        .asObservable()
        .pipe(take(1))
        .toPromise();
      const prevA = de.query(By.css('.pagination > li:first-child > a'));
      expect(prevA.classes.disabled).toBeTruthy();
      prevA.nativeElement.click();
      component.currentPage = 1;
      fixture.detectChanges();
      prevA.nativeElement.click();
      expect(await changePage$).toEqual(0);
    });

    it('next should be disabled on last page', async () => {
      component.currentPage = 3;
      fixture.detectChanges();
      const changePage$ = component.changePage
        .asObservable()
        .pipe(take(1))
        .toPromise();
      const nextA = de.query(By.css('.pagination > li:last-child > a'));
      expect(nextA.classes.disabled).toBeTruthy();
      nextA.nativeElement.click();
      component.currentPage = 2;
      fixture.detectChanges();
      nextA.nativeElement.click();
      expect(await changePage$).toEqual(3);
    });
  });
});
