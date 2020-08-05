import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {SentinelSearchComponent} from './sentinel-search.component';
import {MapModule} from '../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {SentinelSearchQuery} from '../state/sentinel-search/sentinel-search.query';
import {SentinelSearchStore} from '../state/sentinel-search/sentinel-search.store';
import {ReplaySubject} from 'rxjs';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';
import {SentinelSearchFactory, SentinelSearchMetadataFactory} from '../state/sentinel-search/sentinel-search.factory.spec';
import {toTestPromise} from '../../../test.utils.spec';
import environment from 'src/environments/environment';

describe('SentinelSearchComponent', () => {
  let component: SentinelSearchComponent;
  let fixture: ComponentFixture<SentinelSearchComponent>;
  let de: DebugElement;
  let query: SentinelSearchQuery;
  let store: SentinelSearchStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [NoopAnimationsModule, MapModule, RouterTestingModule, HttpClientTestingModule]
    })
      .compileComponents();

    query = TestBed.get(SentinelSearchQuery);
    store = TestBed.get(SentinelSearchStore);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SentinelSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    de = fixture.debugElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('No search results info', () => {
    it('should show depending on the showNoResults$', () => {
      const showNoResults$ = new ReplaySubject<boolean>();
      component.showNoResults$ = showNoResults$.asObservable();
      showNoResults$.next(true);
      fixture.detectChanges();
      expect(de.query(By.css('[data-test="no-results"]'))).toBeTruthy();
      showNoResults$.next(false);
      fixture.detectChanges();
      expect(de.query(By.css('[data-test="no-results"]'))).toBeFalsy();
    });

    describe('results.length', () => {
      beforeEach(() => {
        store.update({
          metadata: SentinelSearchMetadataFactory.build(),
          metadataLoaded: true,
          loaded: true
        });
      });

      it('showNoResults$ should be true if length == 0', async () => {
        store.set([]);
        expect(await toTestPromise(component.showNoResults$)).toBeTruthy();
      });

      it('showNoResults$ should be false if length > 0', async () => {
        store.set(SentinelSearchFactory.buildListSearchResult(1, environment.apiPrefixV1));
        expect(await toTestPromise(component.showNoResults$)).toBeFalsy();
      });
    });

    describe('loaded', () => {
      beforeEach(() => {
        store.update({
          metadata: SentinelSearchMetadataFactory.build(),
          metadataLoaded: true,
        });
        store.set([]);
      });

      it('showNoResults$ should be false if loaded == false', async () => {
        store.update({loaded: false});
        expect(await toTestPromise(component.showNoResults$)).toBeFalsy();
      });

      it('showNoResults$ should be true if loaded == true', async () => {
        store.update({loaded: true});
        expect(await toTestPromise(component.showNoResults$)).toBeTruthy();
      });
    });

    describe('Sentinel Sections', () => {
      beforeEach(() => {
        store.update({
          loaded: true,
          metadataLoaded: true,
        });
        store.set([]);
      });

      it('showNoResults$ should depend on metadata sections length', async () => {
        store.update({
          metadata: {
            common: {
              params: []
            },
            sections: []
          }
        });
        expect(await toTestPromise(component.showNoResults$)).toBeFalsy();
        store.update({metadata: SentinelSearchMetadataFactory.build()});
        expect(await toTestPromise(component.showNoResults$)).toBeTruthy();
      });
    });
  });
});
