import {handleHttpRequest$} from 'src/app/common/store.util';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductStore} from './product.store';
import {
  AVAILABLE_TIMELINE_RESOLUTIONS,
  MostRecentScene,
  Product,
  PRODUCT_MODE_FAVOURITE,
  PRODUCT_MODE_QUERY_KEY,
  TIMELINE_RESOLUTION_QUERY_KEY
} from './product.model';
import {catchError, finalize, take, tap} from 'rxjs/operators';
import {ProductQuery} from './product.query';
import {LegendService} from '../legend/legend.service';
import {Observable, of, throwError} from 'rxjs';
import {SceneStore} from '../scene/scene.store.service';
import {SceneService} from '../scene/scene.service';
import {applyTransaction} from '@datorama/akita';
import {timezone, yyyymm, yyyymmdd} from '../../../../utils/miscellaneous/date-utils';
import {HANDLE_ALL_ERRORS} from '../../../../utils/error-interceptor/error.helper';
import {Router} from '@angular/router';
import environment from 'src/environments/environment';
import * as moment from 'moment';
import {NotificationService} from 'notifications';
import {SceneQuery} from '../scene/scene.query';

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
    private router: Router
  ) {
  }

  get() {
    const url = `${environment.apiPrefixV1}/products`;
    this.http.get<Product[]>(url)
      .pipe(
        handleHttpRequest$(this.store)
      )
      .subscribe((data) => this.store.set(data));
  }

  getLastAvailableScene() {
    const product = this.query.getActive();

    if (product == null) {
      return;
    }

    this.store.ui.update(product.id, state => ({...state, isLoading: true}));
    this.http.get<MostRecentScene>(`${environment.apiPrefixV1}/products/${product.id}/scenes/most-recent`,
      {params: {timeZone: timezone()}}
    ).pipe(
      finalize(() => this.store.ui.update(product.id, state => ({...state, isLoading: false})))
    ).subscribe((data: MostRecentScene) => {
      if (data.sceneId == null) {
        this._notificationService.addGeneral({type: 'info', content: 'Ten produkt nie posiada jeszcze scen'});
        return;
      }

      this.setSelectedDate(data.timestamp);
      this.sceneService.get(product, data.timestamp.substr(0, 10));
      this.sceneService.setActive(data.sceneId);
    });
  }

  setActive(productId: number | null) {
    this.store.ui.update({isLoading: false});

    if (productId != null) {
      this.store.ui.update(productId, {isLoading: true});
      const product = this.query.getEntity(productId);
      this.getSingle$(product)
        .pipe(
          finalize(() => this.store.ui.update(productId, {isLoading: false}))
        ).subscribe(product => {
        applyTransaction(() => {
          this.store.update(state => ({...state, ui: {...state.ui, loadedMonths: [], availableDays: []}}));
          this.store.setActive(productId);
        });
        this.getAvailableDays();
      });
    } else {
      applyTransaction(() => {
        this.store.update(state => ({...state, ui: {...state.ui, loadedMonths: [], availableDays: []}}));
        this.store.setActive(null);
        this.sceneStore.setActive(null);
        this.legendService.set(null);
      });
    }
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

  fetchAvailableDays(dateF: string) {
    const loadedMonths = this.query.getValue().ui.loadedMonths;
    if (loadedMonths.includes(dateF)) {
      return;
    }
    this.store.update(state => ({...state, ui: {...state.ui, loadedMonths: [...state.ui.loadedMonths, dateF]}}));
    this.http.get<string[]>(`${environment.apiPrefixV1}/products/${this.query.getActiveId()}/scenes/available`, {
      params: {tz: timezone(), yearMonth: dateF}
    })
      .subscribe(data => this.updateAvailableDays(data));
  }

  getAvailableDays() {
    const activeProduct: Product = this.query.getActive();
    if (activeProduct == null) {
      return;
    }

    const ui = this.query.getValue().ui;

    const dateF = yyyymm(new Date(ui.selectedYear, ui.selectedMonth, ui.selectedDay));

    this.http.get<string[]>(`${environment.apiPrefixV1}/products/${activeProduct.id}/scenes/available`,
      {params: {timeZone: environment.timezone, yearMonth: dateF}})
      .pipe(
        finalize(() => this.store.setLoading(false)),
        catchError(error => {
          this.store.setError(error);
          return throwError(error);
        }),
      )
      .subscribe(data => {
        this.updateAvailableDays(data);
        this.sceneService.get(activeProduct, ui.selectedDate);
      });
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

  toggleActive(productId: number) {
    if (this.query.getActiveId() == productId) {
      this.setActive(null);
    } else {
      this.setActive(productId);
    }
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
    this.sceneService.get(this.query.getActive(), yyyymmdd(newDate.toDate()), 'last');
  }

  nextDay() {
    let newDate = moment(this.query.getValue().ui.selectedDate);
    newDate.add(1, 'day');
    this.setSelectedDate(yyyymmdd(newDate.toDate()));
    this.sceneService.get(this.query.getActive(), yyyymmdd(newDate.toDate()), 'first');
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

  private getSingle$(product: Product): Observable<any> {
    if (product.legend === undefined) {
      return this.http.get<Product>(`${environment.apiPrefixV1}/products/${product.id}`)
        .pipe(tap(pt => this.store.upsert(product.id, pt)));
    } else {
      return of(null);
    }
  }
}
