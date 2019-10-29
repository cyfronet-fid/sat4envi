import { Injectable } from '@angular/core';
import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Group } from './group.model';

export interface GroupState extends EntityState<Group> {}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Group', idKey: 'slug' })
export class GroupStore extends EntityStore<GroupState, Group> {
  constructor() {
    super();
  }
}

