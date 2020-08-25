import {Legend} from '../legend/legend.model';
import {ActiveState, EntityState} from '@datorama/akita';
import {yyyymmdd} from '../../../../utils/miscellaneous/date-utils';

export const PRODUCT_MODE_QUERY_KEY = 'pmode'
export const PRODUCT_MODE_FAVOURITE = 'favourite'

export interface ProductCategory {
  id: number | undefined;
  label: string;
  url: string;
}

export interface Product {
  id: number | undefined;
  name: string;
  displayName: string;
  imageUrl: string;
  description: string;
  legend: Legend | null | undefined;
  layerName: string;
  favourite: boolean;
  productCategory: ProductCategory;
}

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

export interface ProductUI {
  isLoading: boolean;
  isFavouriteLoading: boolean;
}

export interface ProductUIState extends EntityState<ProductUI> {
}

export function createProductState(state: Partial<ProductState> = {}): ProductState {
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
