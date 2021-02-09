/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {Legend} from '../legend/legend.model';
import {ActiveState, EntityState} from '@datorama/akita';
export const COLLAPSED_CATEGORIES_LOCAL_STORAGE_KEY = 'collapsedCategories';
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
  rank: number;
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
  rank: number;
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
      availableDays: []
    },
    licensedProducts: [],
    loaded: false,
    ...state
  };
}
