import {Injectable} from '@angular/core';
import {Query} from '@datorama/akita';
import {ProfileStore} from './profile.store';
import {ProfileState} from './profile.model';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class ProfileQuery extends Query<ProfileState> {

  constructor(protected store: ProfileStore) {
    super(store);
  }

  public selectMemberZK(): Observable<boolean> {
    return this.select('memberZK');
  }

  public selectCanSeeInstitutions(): Observable<boolean> {
    return this.select().pipe(map(state => state.admin ||
      state.roles.find(role => ['INST_ADMIN', 'INST_MANAGER', 'GROUP_MANAGER'].includes(role.role)) != null));
  }
}
