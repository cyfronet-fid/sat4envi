import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductStore} from './product.store';
import {Product} from './product.model';
import {catchError, finalize, tap} from 'rxjs/operators';
import {ProductQuery} from './product.query';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {LegendService} from '../legend/legend.service';
import {Observable, of, throwError} from 'rxjs';
import {SceneStore} from '../scene/scene.store.service';
import {SceneService} from '../scene/scene.service';
import {applyTransaction} from '@datorama/akita';
import {yyyymm, yyyymmdd} from '../../../../utils/miscellaneous/date-utils';
import {catchErrorAndHandleStore} from '../../../../common/store.util';
import {HANDLE_ALL_ERRORS} from '../../../../utils/error-interceptor/error.helper';

@Injectable({providedIn: 'root'})
export class ProductService {

  constructor(private store: ProductStore,
              private sceneStore: SceneStore,
              private CONFIG: S4eConfig,
              private http: HttpClient,
              private legendService: LegendService,
              private query: ProductQuery,
              private sceneService: SceneService) {
  }

  get() {
    this.sceneStore.setLoading(true);
    this.store.setLoading(true);
    this.http.get<Product[]>(`${this.CONFIG.apiPrefixV1}/products`)
      .pipe(catchErrorAndHandleStore(this.store))
      .subscribe(data => this.store.set(data));
  }

  setActive(productId: number | null) {
    if (productId != null) {
      const product = this.query.getEntity(productId);
      this.getSingle$(product).subscribe((product) => {
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
    (
      isFavourite
        ? this.http
          .put(`${this.CONFIG.apiPrefixV1}/products/${ID}/favourite`, {}, HANDLE_ALL_ERRORS)
        : this.http
          .delete(`${this.CONFIG.apiPrefixV1}/products/${ID}/favourite`, HANDLE_ALL_ERRORS)
    )
      .pipe(catchErrorAndHandleStore(this.store))
      .subscribe(() => this.store.setLoading(false));
  }

  fetchAvailableDays(dateF: string) {
    const loadedMonths = this.query.getValue().ui.loadedMonths;
    if (loadedMonths.includes(dateF)) {
      return;
    }
    this.store.update(state => ({...state, ui: {...state.ui, loadedMonths: [...state.ui.loadedMonths, dateF]}}));
    this.http.get<string[]>(`${this.CONFIG.apiPrefixV1}/products/${this.query.getActiveId()}/scenes/available`, {
      params: {tz: this.CONFIG.timezone, yearMonth: dateF}
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

    this.http.get<string[]>(`${this.CONFIG.apiPrefixV1}/products/${activeProduct.id}/scenes/available`,
      {params: {timeZone: this.CONFIG.timezone, yearMonth: dateF}})
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

  setSelectedDate($event: string) {
    const date = new Date($event);

    this.store.update(store => ({
      ...store,
      ui: {
        ...store.ui,
        selectedMonth: date.getMonth(),
        selectedYear: date.getFullYear(),
        selectedDay: date.getDate(),
        selectedDate: yyyymmdd(date)
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

  private getSingle$(product: Product): Observable<any> {
    if (product.legend === undefined) {
      return this.http.get<Product>(`${this.CONFIG.apiPrefixV1}/products/${product.id}`)
        .pipe(tap(pt => this.store.upsert(product.id, pt)));
    } else {
      return of(null);
    }
  }
}
