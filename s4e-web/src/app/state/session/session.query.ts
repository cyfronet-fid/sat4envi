import {Injectable} from '@angular/core';
import {Query} from '@datorama/akita';
import {SessionStore} from './session.store';
import {Role, Session} from './session.model';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

const ROLE_INSTANCE_ADMIN = 'INST_ADMIN';
const ROLE_INSTANCE_MANAGER = 'INST_MANAGER';
const ROLE_GROUP_MANAGER = 'GROUP_MANAGER';

const MANAGER_ROLES = [ROLE_INSTANCE_ADMIN, ROLE_INSTANCE_MANAGER, ROLE_GROUP_MANAGER];

function hasAnyManagerRole(roles: Role[]) {
  return roles.some(role => MANAGER_ROLES.includes(role.role));
}

@Injectable({
  providedIn: 'root'
})
export class SessionQuery extends Query<Session> {
  constructor(protected store: SessionStore) {
    super(store);
  }

  isLoggedIn(): boolean {
    return this.getValue().email != null;
  }

  isLoggedIn$(): Observable<boolean> {
    return this.select('email').pipe(map(email => email != null));
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
  getAdministratorInstitutionsSlugs() {
    const institutionsRolesMap = this._getInstitutionsRolesMap();
    return Object.keys(institutionsRolesMap)
      .filter(institutionSlug => hasAnyManagerRole(institutionsRolesMap[institutionSlug]));
  }
  getMemberInstitutionSlugs() {
    const institutionsRolesMap = this._getInstitutionsRolesMap();
    return Object.keys(institutionsRolesMap)
      .filter(institutionSlug => !hasAnyManagerRole(institutionsRolesMap[institutionSlug]));
  }

  public selectMemberZK(): Observable<boolean> {
    return this.select('memberZK');
  }

  public selectCanSeeInstitutions(): Observable<boolean> {
    return this.select().pipe(map(state => state.admin || hasAnyManagerRole(state.roles)));
  }

  private _getInstitutionsRolesMap() {
    return this.getValue().roles
      .reduce((permissions, role) => {
          return Object.keys(permissions).includes(role.institutionSlug)
              ? {...permissions, [role.institutionSlug]: [...permissions[role.institutionSlug], role]}
              : {...permissions, [role.institutionSlug]: [role]};
        },
        {}
      );
  }
}
