import {Legend} from '../legend/legend.model';
import {ActiveState, EntityState} from '@datorama/akita';
export const COLLAPSED_CATEGORIES_LOCAL_STORAGE_KEY = 'collapsedCategories'
export const PRODUCT_MODE_QUERY_KEY = 'pmode';
export const PRODUCT_MODE_FAVOURITE = 'favourite';
export const TIMELINE_RESOLUTION_QUERY_KEY = 'resolution';
export const AVAILABLE_TIMELINE_RESOLUTIONS = [1, 3, 6, 12, 24];
export const DEFAULT_TIMELINE_RESOLUTION = 24;

export const MAXIMUM_SCENE_TIME_DISTANCE = 60000;

export interface MostRecentScene {
  sceneId: number;
  timestamp: string;
}

export interface ProductCategory {
  id: number | undefined;
  label: string;
  iconPath: string;
}

export interface UIProductCategory extends ProductCategory {
  collapsed: boolean;
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

export interface ProductWithUICategory extends Product {
  productCategory: UIProductCategory;
}

export interface LicensedProduct {
  institutionsSlugs: string[];
  productId: string;
  productName: string;

  // IMPORTANT!!! This field tells if specific institution has this product licence
  // Field provided to reduce calculate complexity in HTML template
  hasInstitutionLicence?: boolean;
}

export interface ProductState extends EntityState<Product>, ActiveState<number> {
    ui: {
        loadedMonths: string[];
        selectedDate: string;
        selectedDay: number;
        selectedYear: number;
        selectedMonth: number;
        availableDays: string[];
        manuallySelectedDate: string;
    };
    licensedProducts: LicensedProduct[];
    loaded: boolean;
}

export interface ProductUI {
  isLoading: boolean;
  isFavouriteLoading: boolean;
}

export interface ProductUIState extends EntityState<ProductUI> {
  collapsedCategories: number[];
}

export function createProductState(state: Partial<ProductState> = {}): ProductState {
    return {
        active: null,
        error: null,
        loading: true,
        ui: {
            loadedMonths: [],
            selectedDate: null,
            selectedDay: null,
            selectedYear: null,
            selectedMonth: null,
            manuallySelectedDate: null,
            availableDays: [],
        },
      licensedProducts: [],
        loaded: false,
        ...state
    };
}
