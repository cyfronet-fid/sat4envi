import {SceneQuery} from './scene.query';
import {SceneStore} from './scene.store.service';
import {TestBed} from '@angular/core/testing';
import {ProductQuery} from '../product/product.query';
import {ProductStore} from '../product/product.store';
import {ProductFactory} from '../product/product.factory.spec';
import {SceneFactory} from './scene.factory.spec';
import {take, toArray} from 'rxjs/operators';
import {ReplaySubject} from 'rxjs';
import moment from 'moment-timezone';
import * as dateUtil from '../../../../utils/miscellaneous/date-utils';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';

describe('SceneQuery', () => {
  let query: SceneQuery;
  let store: SceneStore;
  let productQuery: ProductQuery;
  let productStore: ProductStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LocalStorageTestingProvider,
        SceneStore,
        SceneQuery,
        ProductQuery,
        ProductStore],
    });

    productQuery = TestBed.get(ProductQuery);
    productStore = TestBed.get(ProductStore);
    store = TestBed.get(SceneStore);
    query = TestBed.get(SceneQuery);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

  it('selectTimelineUI', async () => {
    spyOn(dateUtil, 'timezone').and.returnValue('Europe/Warsaw');
    moment.tz.setDefault(dateUtil.timezone());

    const timelineResolutions$ = new ReplaySubject(1);
    spyOn(productQuery, 'selectTimelineResolution').and.returnValue(timelineResolutions$);
    timelineResolutions$.next(24);
    const product = ProductFactory.build();
    productStore.set([product]);
    productStore.setActive(product.id);
    productStore.update(state => ({
      ...state,
      ui: {
        ...state.ui,
        selectedDate: '2020-08-01',
        selectedDay: 1,
        selectedMonth: 7,
        selectedYear: 2020,
      }
    }));
    const scene1 = SceneFactory.build({timestamp: '2020-08-01T01:00:00.000Z'});
    const scene2 = SceneFactory.build({timestamp: '2020-08-01T21:00:00.000Z'});
    store.set([scene1, scene2]);

    const timelinePromise = query.selectTimelineUI().pipe(take(3), toArray()).toPromise();
    timelineResolutions$.next(12);
    store.setActive(scene2.id);

    expect(await timelinePromise).toEqual([{
      scenes: [
        {...scene1, position: 12.5},
        {...scene2, position: 95.83333333333334}
        ],
      startTime: '2020-07-31T22:00:00.000Z'
    }, {
      scenes: [{...scene1, position: 25}],
      startTime: '2020-07-31T22:00:00.000Z'
    }, {
      scenes: [{...scene2, position: 91.66666666666666}],
      startTime: '2020-08-01T10:00:00.000Z'
    }]);
  });
});
