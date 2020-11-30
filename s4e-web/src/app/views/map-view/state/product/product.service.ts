import {handleHttpRequest$} from 'src/app/common/store.util';
import {Inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductStore} from './product.store';
import {
  AVAILABLE_TIMELINE_RESOLUTIONS,
  COLLAPSED_CATEGORIES_LOCAL_STORAGE_KEY,
  MostRecentScene,
  Product,
  PRODUCT_MODE_FAVOURITE,
  PRODUCT_MODE_QUERY_KEY,
  TIMELINE_RESOLUTION_QUERY_KEY
} from './product.model';
import {filter, finalize, switchMap, take, tap} from 'rxjs/operators';
import {ProductQuery} from './product.query';
import {LegendService} from '../legend/legend.service';
import {forkJoin, Observable, of} from 'rxjs';
import {SceneStore} from '../scene/scene.store.service';
import {SceneService} from '../scene/scene.service';
import {applyTransaction, arrayRemove, arrayUpsert} from '@datorama/akita';
import {timezone, yyyymm, yyyymmdd} from '../../../../utils/miscellaneous/date-utils';
import {HANDLE_ALL_ERRORS} from '../../../../utils/error-interceptor/error.helper';
import {Router} from '@angular/router';
import environment from 'src/environments/environment';
import * as moment from 'moment';
import {NotificationService} from 'notifications';
import {SceneQuery} from '../scene/scene.query';
import {LocalStorage} from '../../../../app.providers';
import {logIt} from '../../../../utils/rxjs/observable';

@Injectable({providedIn: 'root'})
export class ProductService {

  constructor(
    private store: ProductStore,
    private sceneStore: SceneStore,
    private http: HttpClient,
    private legendService: LegendService,
    private query: ProductQuery,
    private _notificationService: NotificationService,
    private sceneService: SceneService,
    private _sceneQuery: SceneQuery,
    private router: Router,
    @Inject(LocalStorage) private storage: Storage
  ) {
  }

  get() {
    return this.http.get<Product[]>(`${environment.apiPrefixV1}/products`)
      .pipe(
        handleHttpRequest$(this.store),
        tap(data => this.store.set(data))
      );
  }

  getLastAvailableScene$() {
    const product = this.query.getActive();
    if (!product) {
      return of(null);
    }

    const url = `${environment.apiPrefixV1}/products/${product.id}/scenes/most-recent`;
    const params = {params: {timeZone: timezone()}};

    return this.http.get<MostRecentScene>(url, params)
      .pipe(
        tap(() => this.store.ui.update(product.id, state => ({...state, isLoading: true}))),
        filter(data => {
          if (data.sceneId == null) {
            this._notificationService.addGeneral({type: 'info', content: 'Ten produkt nie posiada jeszcze scen'});
          }

          return !!data.sceneId;
        }),
        tap(data => this.setSelectedDate(data.timestamp)),
        switchMap(data => forkJoin([of(data), this.sceneService.get(product, data.timestamp.substr(0, 10))])),
        tap(([data]) => this.sceneService.setActive(data.sceneId)),
        tap(() => this.store.ui.update(product.id, state => ({...state, isLoading: false})))
      );
  }

  setActive$(productId: number | null) {
    this.store.ui.update({isLoading: false});
    if (productId == null) {
      applyTransaction(() => {
        this.store.update(state => ({...state, ui: {...state.ui, loadedMonths: [], availableDays: []}}));
        this.store.setActive(null);
        this.sceneStore.setActive(null);
        this.legendService.set(null);
      });

      return of(null);
    }
    this.legendService.set(null);

    return this.query.selectEntity(productId)
      .pipe(
        tap(() => this.store.ui.update(productId, {isLoading: true})),
        switchMap(product => this.getSingle$(product)),
        tap(() => applyTransaction(() => {
          this.store.update(state => ({...state, ui: {...state.ui, loadedMonths: [], availableDays: []}}));
          this.store.setActive(productId);
        })),
        switchMap((product) => {
          const ui = this.query.getValue().ui;
          const date = yyyymm(new Date(ui.selectedYear, ui.selectedMonth, ui.selectedDay));
          return forkJoin([of(product), this.fetchAvailableDays$(date)]);
        }),
        switchMap(([product, any]) => {
          const ui = this.query.getValue().ui;
          return this.sceneService.get(product, ui.selectedDate);
        }),
        tap(() => this.store.ui.update(productId, {isLoading: false}))
      );
  }

  setDateRange(month: number, year: number) {

  }

  toggleFavourite(ID: number, isFavourite: boolean) {
    this.store.ui.update(ID, {isFavouriteLoading: true});

    const request$ = (
      isFavourite
        ? this.http
          .put(`${environment.apiPrefixV1}/products/${ID}/favourite`, {}, HANDLE_ALL_ERRORS)
        : this.http
          .delete(`${environment.apiPrefixV1}/products/${ID}/favourite`, HANDLE_ALL_ERRORS)
    )
      .pipe(
        handleHttpRequest$(this.store),
        finalize(() => {
          this.store.ui.update(ID, {isFavouriteLoading: false});
          this.store.update(ID, {favourite: isFavourite});
        })
      )
      .subscribe();
  }

  fetchAvailableDays$(dateF: string) {
    const loadedMonths = this.query.getValue().ui.loadedMonths;
    if (loadedMonths.includes(dateF)) {
      return of([]);
    }

    this.store.update(state => ({...state, ui: {...state.ui, loadedMonths: [...state.ui.loadedMonths, dateF]}}));
    const url = `${environment.apiPrefixV1}/products/${this.query.getActiveId()}/scenes/available`;
    const params = {params: {tz: timezone(), yearMonth: dateF}};
    return this.http.get<string[]>(url, params)
      .pipe(
        handleHttpRequest$(this.store),
        tap(data => this.updateAvailableDays(data))
      );
  }

  setSelectedDate(dateString: string) {
    const date = new Date(dateString);

    this.store.update(store => ({
      ...store,
      ui: {
        ...store.ui,
        selectedMonth: date.getMonth(),
        selectedYear: date.getFullYear(),
        selectedDay: date.getDate(),
        selectedDate: yyyymmdd(date),
      }
    }));
  }

  updateAvailableDays(data: string[]) {
    this.store.update(state => {
      const days = [...data.filter(d => !state.ui.availableDays.includes(d)), ...state.ui.availableDays];
      return {...state, ui: {...state.ui, availableDays: days}};
    });
  }

  setFavouriteMode(favourite: boolean) {
    this.router.navigate([], {
      queryParamsHandling: 'merge',
      queryParams: {[PRODUCT_MODE_QUERY_KEY]: favourite ? PRODUCT_MODE_FAVOURITE : ''}
    });
  }

  nextScene() {
    if (!this.sceneService.next()) {
      this.nextDay();
    }
  }

  previousScene() {
    if (!this.sceneService.previous()) {
      this.previousDay();
    }
  }

  previousDay() {
    let newDate = moment(this.query.getValue().ui.selectedDate);
    newDate.subtract(1, 'day');
    this.setSelectedDate(yyyymmdd(newDate.toDate()));
    this.sceneService.get(this.query.getActive(), yyyymmdd(newDate.toDate()), 'last').subscribe();
  }

  nextDay() {
    let newDate = moment(this.query.getValue().ui.selectedDate);
    newDate.add(1, 'day');
    this.setSelectedDate(yyyymmdd(newDate.toDate()));
    this.sceneService.get(this.query.getActive(), yyyymmdd(newDate.toDate()), 'first').subscribe();
  }

  moveResolution(direction: -1 | 1) {
    this.query.selectTimelineResolution().pipe(
      take(1)
    ).subscribe(
      currentResolution => {
        const index = AVAILABLE_TIMELINE_RESOLUTIONS.indexOf(currentResolution) + direction;

        if (index < 0 || index >= AVAILABLE_TIMELINE_RESOLUTIONS.length) {
          return;
        }

        this.router.navigate([],
          {
            queryParams: {[TIMELINE_RESOLUTION_QUERY_KEY]: AVAILABLE_TIMELINE_RESOLUTIONS[index]},
            queryParamsHandling: 'merge'
          });
      }
    );
  }

  toggleCategoryCollapse(category: number) {
    this.store.ui.update(state => {
      let categories = state.collapsedCategories.indexOf(category) === -1
        ? arrayUpsert(state.collapsedCategories, category, category)
        : arrayRemove(state.collapsedCategories, category);

      this.storage.setItem(COLLAPSED_CATEGORIES_LOCAL_STORAGE_KEY, JSON.stringify(categories));

      return {...state, collapsedCategories: categories};
    });
  }

  private getSingle$(product: Product): Observable<Product> {
    return (product.description == null
      ? this.http.get<Product>(`${environment.apiPrefixV1}/products/${product.id}`)
        .pipe(tap(pt => this.store.upsert(product.id, pt)))
      : of(product)).pipe(tap(pt => this.legendService.set(pt.legend)))

  }
}
