import { Role } from './../session/session.model';
import { Injectable } from '@angular/core';
import {Query} from '@datorama/akita';
import {ProfileStore} from './profile.store';
import {ProfileState} from './profile.model';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

const ROLE_INSTANCE_ADMIN = 'INST_ADMIN';
const ROLE_INSTANCE_MANAGER = 'INST_MANAGER';
const ROLE_GROUP_MANAGER = 'GROUP_MANAGER';

const MANAGER_ROLES = [ROLE_INSTANCE_ADMIN, ROLE_INSTANCE_MANAGER, ROLE_GROUP_MANAGER];

const hasAnyManagerRole = (roles: Role[]) => !!roles
  .find(role => MANAGER_ROLES.includes(role.role));

@Injectable({providedIn: 'root'})
export class ProfileQuery extends Query<ProfileState> {
  constructor(protected store: ProfileStore) {
    super(store);
  }

  isAdmin() {
    return this.getValue().admin;
  }
  isManager() {
    return hasAnyManagerRole(this.getValue().roles);
  }
  hasOnlyGroupMemberRole() {
    const roles = this.getValue().roles;
    return roles.length === 1 && roles.some((role) => role.role === ROLE_GROUP_MANAGER);
  }

  public selectMemberZK(): Observable<boolean> {
    return this.select('memberZK');
  }

  public selectCanSeeInstitutions(): Observable<boolean> {
    return this.select().pipe(map(state => state.admin || hasAnyManagerRole(state.roles)));
  }
}
