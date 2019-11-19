import {Injectable} from '@angular/core';
import {ActiveState, EntityState, EntityStore, HashMap, StoreConfig} from '@datorama/akita';
import {ProductType} from './product-type.model';
import moment from 'moment';

export interface ProductTypeState extends EntityState<ProductType>, ActiveState<number> {
  ui: {
    loadedMonths: string[];
    selectedDate: string;
    selectedDay: number;
    selectedYear: number;
    selectedMonth: number;
    availableDays: string[];
  }
}

export function createProductType(state: Partial<ProductTypeState> = {}): ProductTypeState {
  return {
    active: null,
    error: null,
    loading: false,
    ui: {
      loadedMonths: [],
      selectedDate: moment.utc().format('YYYY-MM-DD'),
      selectedDay: moment.utc().day(),
      selectedYear: moment.utc().year(),
      selectedMonth: moment.utc().month() + 1,
      availableDays: []
    },
    ...state
  };
}


@Injectable({providedIn: 'root'})
@StoreConfig({name: 'ProductType'})
export class ProductTypeStore extends EntityStore<ProductTypeState, ProductType, number> {
  constructor() {
    super(createProductType());
  }
}

