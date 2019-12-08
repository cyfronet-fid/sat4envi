import {Injectable} from '@angular/core';
import {ActiveState, EntityState, EntityStore, StoreConfig} from '@datorama/akita';
import {Product} from './product.model';
import moment from 'moment';

export interface ProductState extends EntityState<Product>, ActiveState<number> {
  ui: {
    loadedMonths: string[];
    selectedDate: string;
    selectedDay: number;
    selectedYear: number;
    selectedMonth: number;
    availableDays: string[];
  };
  loaded: boolean;
}

export function createProduct(state: Partial<ProductState> = {}): ProductState {
  return {
    active: null,
    error: null,
    loading: true,
    ui: {
      loadedMonths: [],
      selectedDate: moment.utc().format('YYYY-MM-DD'),
      selectedDay: moment.utc().day(),
      selectedYear: moment.utc().year(),
      selectedMonth: moment.utc().month() + 1,
      availableDays: []
    },
    loaded: false,
    ...state
  };
}


@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Product'})
export class ProductStore extends EntityStore<ProductState, Product, number> {
  constructor() {
    super(createProduct());
  }
}

