import { AkitaGuidService } from 'src/app/views/map-view/state/search-results/guid.service';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {GroupStore} from './group.store';
import {IPageableResponse} from '../../../../state/pagable.model';
import {Institution} from '../../state/institution/institution.model';
import {finalize, map} from 'rxjs/operators';
import {GroupQuery} from './group.query';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {Group, GroupForm} from './group.model';
import {forkJoin, Observable} from 'rxjs';
import {Person} from '../../people/state/person.model';
import {catchErrorAndHandleStore, httpPutRequest$, httpGetRequest$, httpPostRequest$, httpDeleteRequest$} from '../../../../common/store.util';

@Injectable({providedIn: 'root'})
export class GroupService {

  constructor(
    private store: GroupStore,
    private query: GroupQuery,
    private config: S4eConfig,
    private http: HttpClient,
    private _akitaGuidService: AkitaGuidService
  ) {}

  fetchAll(institutionSlug: string) {
    const url = `${this.config.apiPrefixV1}/institutions/${institutionSlug}/groups`;
    return httpGetRequest$<IPageableResponse<Partial<Group>>>(this.http, url, this.store)
      .subscribe(pageable => {
        const groups = pageable.content
          .map(group => ({...group, id: this._akitaGuidService.guid()})) as Group[];
        this.store.set(groups);
      });
  }

  update$(institutionSlug: string, groupSlug: string, value: GroupForm) {
    const url = `${this.config.apiPrefixV1}/institutions/${institutionSlug}/groups/${groupSlug}`;
    const membersEmails = value.membersEmails._;
    return httpPutRequest$(this.http, url, {...value, membersEmails}, this.store);
  }

  create$(institutionSlug: string, value: GroupForm) {
    const url = `${this.config.apiPrefixV1}/institutions/${institutionSlug}/groups`;
    const membersEmails = value.membersEmails._;
    return httpPostRequest$(this.http, url, {...value, membersEmails}, this.store);
  }

  delete$(institutionSlug: string, group: Group) {
    const url = `${this.config.apiPrefixV1}/institutions/${institutionSlug}/groups/${group.slug}`;
    return httpDeleteRequest$(this.http, url, this.store)
      .pipe(finalize(() => this.store.remove(group.id)));
  }

  fetchForm$(institutionSlug: string, groupSlug: string): Observable<GroupForm> {
    this.store.setLoading(true);
    return forkJoin([
      this.http.get<Group>(`${this.config.apiPrefixV1}/institutions/${institutionSlug}/groups/${groupSlug}`),
      this.http.get<{ members: Person[] }>(`${this.config.apiPrefixV1}/institutions/${institutionSlug}/groups/${groupSlug}/members`)
    ]).pipe(
      finalize(() => this.store.setLoading(false)),
      catchErrorAndHandleStore(this.store),
      map(([group, users]) => ({
        name: group.name,
        description: '',
        membersEmails: {_: users.members.map(user => user.email)}
      })));
  }

  delete(instSlug: string, groupSlug: string) {
    const url = `${this.config.apiPrefixV1}/institutions/${instSlug}/groups/${groupSlug}`;
    return httpDeleteRequest$(this.http, url, this.store)
      .subscribe(() => this.store.remove(groupSlug));
  }
}
