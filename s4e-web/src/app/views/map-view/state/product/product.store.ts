import {Injectable} from '@angular/core';
import {ActiveState, EntityState, EntityStore, StoreConfig} from '@datorama/akita';
import {Product} from './product.model';
import {yyyymmdd} from '../../../../utils/miscellaneous/date-utils';

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
  const now = new Date();
  return {
    active: null,
    error: null,
    loading: true,
    ui: {
      loadedMonths: [],
      selectedDate: yyyymmdd(now),
      selectedDay: now.getDate(),
      selectedYear: now.getFullYear(),
      selectedMonth: now.getMonth(),
      availableDays: []
    },
    loaded: false,
    ...state
  };
}


@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Product'})
export class ProductStore extends EntityStore<ProductState, Product> {
  constructor() {
    super(createProduct());
  }
}

