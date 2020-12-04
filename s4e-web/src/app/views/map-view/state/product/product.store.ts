import {Inject, Injectable} from '@angular/core';
import {EntityStore, EntityUIStore, StoreConfig} from '@datorama/akita';
import {COLLAPSED_CATEGORIES_LOCAL_STORAGE_KEY, createProductState, Product, ProductState, ProductUIState} from './product.model';
import {LocalStorage} from '../../../../app.providers';
import {yyyymmdd} from '../../../../utils/miscellaneous/date-utils';


@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Product'})
export class ProductStore extends EntityStore<ProductState, Product> {
  public readonly ui: EntityUIStore<ProductUIState>;

  constructor(@Inject(LocalStorage) storage: Storage) {
    super(createProductState());
    this.createUIStore({collapsedCategories: JSON.parse(storage.getItem(COLLAPSED_CATEGORIES_LOCAL_STORAGE_KEY)) || []}).setInitialEntityState({isLoading: false, isFavouriteLoading: false});
  }

  setSelectedDate(dateString: string, manualTrigger: boolean = false) {
    const date = new Date(dateString);

    this.update(store => ({
      ...store,
      ui: {
        ...store.ui,
        selectedMonth: date.getMonth(),
        selectedYear: date.getFullYear(),
        selectedDay: date.getDate(),
        selectedDate: yyyymmdd(date),
        manuallySelectedDate: manualTrigger ? dateString : store.ui.manuallySelectedDate
      }
    }));
  }
}

