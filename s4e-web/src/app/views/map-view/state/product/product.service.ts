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
import {action, applyTransaction} from '@datorama/akita';

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
    this.http.get<Product[]>(`${this.CONFIG.apiPrefixV1}/products`).subscribe(
      data => this.store.set(data),
      error => this.store.setError(error)
    );
  }

  setActive(productId: number | null) {
    if (productId != null) {
      const product = this.query.getEntity(productId);
      applyTransaction(() => {
        this.store.update(state => ({...state, ui: {...state.ui, loadedMonths: [], availableDays: []}}));
        this.store.setActive(productId);
      });
      console.log(`productService.setActive(${product})`);
      this.getSingle$(product).subscribe(() => this.getAvailableDays());
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

  fetchAvailableDays(dateF: string) {
    const loadedMonths = this.query.getValue().ui.loadedMonths;
    if (loadedMonths.includes(dateF)) {
      return;
    }
    this.store.update(state => ({...state, ui: {...state.ui, loadedMonths: [...state.ui.loadedMonths, dateF]}}));
    this.http.get<string[]>(`${this.CONFIG.apiPrefixV1}/products/${this.query.getActiveId()}/scenes/available?yearMonth=${dateF}`)
      .subscribe(data => this.updateAvailableDays(data));
  }

  getAvailableDays() {
    const activeProductId: number = this.query.getActiveId() as number;
    if (activeProductId == null) {
      return;
    }

    const ui = this.query.getValue().ui;

    const dateF = moment({year: ui.selectedYear, month: ui.selectedMonth}).format('YYYY-MM');

    this.http.get<string[]>(`${this.CONFIG.apiPrefixV1}/products/${activeProductId}/scenes/available?yearMonth=${dateF}`)
      .pipe(finalize(() => this.store.setLoading(false)))
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

    this.store.update(store => ({
      ...store,
      ui: {
        ...store.ui,
        selectedMonth: date.month(),
        selectedYear: date.year(),
        selectedDay: date.day(),
        selectedDate: $event
      }
    }));
  }

  updateAvailableDays(data: string[]) {
    this.store.update(state => {
      const days = [...data.filter(d => !state.ui.availableDays.includes(d)), ...state.ui.availableDays];
      return {...state, ui: {...state.ui, availableDays: days}};
    });
  }

  private getSingle$(product: Product): Observable<any> {
    if (product.legend === undefined) {
      return this.http.get<Product>(`${this.CONFIG.apiPrefixV1}/products/${product.id}`)
        .pipe(tap(pt => this.store.upsert(product.id, pt)));
    } else {
      return of(null);
    }
  }

  toggleActive(productId: number) {
    if(this.query.getActiveId() == productId) {
      this.setActive(null);
    } else {
      this.setActive(productId);
    }
  }
}
