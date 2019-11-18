import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { GroupStore, GroupState } from './group.store';
import { Group } from './group.model';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {DEFAULT_GROUP_SLUG} from '../../people/state/person.model';

@Injectable({
  providedIn: 'root'
})
export class GroupQuery extends QueryEntity<GroupState, Group> {

  constructor(protected store: GroupStore) {
    super(store);
  }

  selectAllWithoutDefault(): Observable<Group[]> {
    return this.selectAll().pipe(map(groups => groups.filter(g => g.slug !== DEFAULT_GROUP_SLUG)));
  }
}
