import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductStore} from './product.store';
import {Product} from './product.model';
import {finalize, tap} from 'rxjs/operators';
import {ProductQuery} from './product.query';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {LegendService} from '../legend/legend.service';
import {Observable, of} from 'rxjs';
import moment from 'moment';
import {SceneStore} from '../scene/scene.store.service';
import {SceneService} from '../scene/scene.service';

@Injectable({providedIn: 'root'})
export class ProductService {

  constructor(private productStore: ProductStore,
              private sceneStore: SceneStore,
              private CONFIG: S4eConfig,
              private http: HttpClient,
              private legendService: LegendService,
              private productQuery: ProductQuery,
              private sceneService: SceneService) {
  }

  get() {
    this.sceneStore.setLoading(true);
    this.http.get<Product[]>(`${this.CONFIG.apiPrefixV1}/productTypes`).pipe(
      finalize(() => this.sceneStore.setLoading(false)),
    ).subscribe(data => this.productStore.set(data));
  }

  setActive(productId: number | null) {
    this.productStore.update(state => ({...state, ui: {...state.ui, loadedMonths: [], availableDays: []}}));
    if (productId != null && this.productQuery.getActiveId() !== productId) {
      const product = this.productQuery.getEntity(productId);
      this.productStore.setActive(productId);
      this.getSingle$(product)
        .subscribe(() => this.getAvailableDays());
    } else {
      this.productStore.setActive(null);
      this.sceneStore.setActive(null);
      this.legendService.set(null);
    }
  }

  setDateRange(month: number, year: number) {

  }

  fetchAvailableDays(dateF: string) {
    const loadedMonths = this.productQuery.getValue().ui.loadedMonths;
    if (loadedMonths.includes(dateF)) {
      return;
    }
    this.productStore.update(state => ({...state, ui: {...state.ui, loadedMonths: [...state.ui.loadedMonths, dateF]}}));
    this.http.get<string[]>(`${this.CONFIG.apiPrefixV1}/products/productTypeId/${this.productQuery.getActiveId()}/available?yearMonth=${dateF}`)
      .subscribe(data => this.updateAvailableDays(data));
  }

  getAvailableDays() {
    const activeProductId: number = this.productQuery.getActiveId() as number;
    if (activeProductId == null) {
      return;
    }

    const ui = this.productQuery.getValue().ui;

    const dateF = moment({year: ui.selectedYear, month: ui.selectedMonth}).format('YYYY-MM');

    this.http.get<string[]>(`${this.CONFIG.apiPrefixV1}/products/productTypeId/${activeProductId}/available?yearMonth=${dateF}`)
      .pipe(finalize(() => this.productStore.setLoading(false)))
      .subscribe(data => {
        this.updateAvailableDays(data);
        this.sceneService.get(activeProductId, moment.utc({
          year: ui.selectedYear,
          month: ui.selectedMonth,
          day: ui.selectedDay
        }).format('YYYY-MM-DD'));
      });
  }

  setSelectedDate($event: string) {
    const date = moment.utc($event, 'YYYY-MM-DD');

    this.productStore.update(store => ({
      ...store,
      ui: {...store.ui, selectedMonth: date.month(), selectedYear: date.year(), selectedDay: date.day(), selectedDate: $event}
    }));
  }

  updateAvailableDays(data: string[]) {
    this.productStore.update(state => {
      const days = [...data.filter(d => !state.ui.availableDays.includes(d)), ...state.ui.availableDays];
      return {...state, ui: {...state.ui, availableDays: days}};
    });
  }

  private getSingle$(product: Product): Observable<any> {
    if (product.legend === undefined) {
      return this.http.get<Product>(`${this.CONFIG.apiPrefixV1}/productTypes/${product.id}`)
        .pipe(tap(pt => this.productStore.upsert(product.id, pt)));
    } else {
      return of(null);
    }
  }
}
