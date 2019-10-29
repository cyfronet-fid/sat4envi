import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductTypeStore} from './product-type.store';
import {ProductType} from './product-type.model';
import {finalize, tap} from 'rxjs/operators';
import {ProductService} from '../product/product.service';
import {ProductTypeQuery} from './product-type.query';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {ProductStore} from '../product/product.store';
import {LegendService} from '../legend/legend.service';
import {Observable, of} from 'rxjs';
import moment from 'moment';

@Injectable({providedIn: 'root'})
export class ProductTypeService {

  constructor(private store: ProductTypeStore,
              private productStore: ProductStore,
              private CONFIG: S4eConfig,
              private http: HttpClient,
              private legendService: LegendService,
              private query: ProductTypeQuery,
              private productService: ProductService) {
  }

  get() {
    this.productStore.setLoading(true);
    this.http.get<ProductType[]>(`${this.CONFIG.apiPrefixV1}/productTypes`).pipe(
      finalize(() => this.productStore.setLoading(false)),
    ).subscribe(data => this.store.set(data));
  }

  setActive(productTypeId: number | null) {
    this.store.update(state => ({...state, ui: {...state.ui, loadedMonths: []}}));
    if (productTypeId != null && this.query.getActiveId() !== productTypeId) {
      const productType = this.query.getEntity(productTypeId);
      let precondition: Observable<any>|null = null;
      this.store.setActive(productTypeId);
      this.getSingle$(productType)
        .subscribe(() => this.getAvailableDays());
    } else {
      this.store.setActive(null);
      this.productStore.setActive(null);
      this.legendService.set(null);
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
    this.http.get<string[]>(`${this.CONFIG.apiPrefixV1}/products/productTypeId/${this.query.getActiveId()}/available?yearMonth=${dateF}`)
      .subscribe(data => this.store.update(state => ({...state, ui: {...state.ui, availableDays: data.reduce((acc, val) => {acc[val] = true; return acc;}, {})}})));
  }

  getAvailableDays() {
    const activeProductTypeId: number = this.query.getActiveId() as number;
    if (activeProductTypeId == null) {return;}

    const ui = this.query.getValue().ui;

    const dateF = moment({year: ui.selectedYear, month: ui.selectedMonth}).format('YYYY-MM');

    this.http.get<string[]>(`${this.CONFIG.apiPrefixV1}/products/productTypeId/${activeProductTypeId}/available?yearMonth=${dateF}`)
      .pipe(finalize(() => this.store.setLoading(false)))
      .subscribe(data => {
        this.store.update(state => ({...state, ui: {...state.ui, availableDays: data.reduce((acc, val) => {acc[val] = true; return acc;}, {})}}));
        this.productService.get(activeProductTypeId, moment.utc({year: ui.selectedYear, month: ui.selectedMonth, day: ui.selectedDay}).format('YYYY-MM-DD'));
      });
  }

  private getSingle$(productType: ProductType): Observable<any> {
    if (productType.legend === undefined) {
      return this.http.get<ProductType>(`${this.CONFIG.apiPrefixV1}/productTypes/${productType.id}`)
        .pipe(tap(pt => this.store.upsert(productType.id, pt)));
    } else {
      return of(null);
    }
  }

  setSelectedDate($event: string) {
    const date = moment.utc($event, 'YYYY-MM-DD');

    this.store.update(store => ({...store, ui: {...store.ui, selectedMonth: date.month(), selectedYear: date.year(), selectedDay: date.day(), selectedDate: $event}}));
  }
}
