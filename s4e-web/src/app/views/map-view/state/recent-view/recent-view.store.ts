import { Injectable } from '@angular/core';
import { ActiveState, EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { RecentView } from './recent-view.model';

export interface RecentViewState extends EntityState<RecentView>, ActiveState {}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'RecentView', idKey: 'productTypeId' })
export class RecentViewStore extends EntityStore<RecentViewState, RecentView> {

  constructor() {
    super({
      loading: false
    });
  }
}

