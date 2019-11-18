import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {GroupStore} from './group.store';
import {IPageableResponse} from '../../../../state/pagable.model';
import {Institution} from '../../state/institution.model';
import {catchError, combineAll, finalize, map, tap} from 'rxjs/operators';
import {GroupQuery} from './group.query';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {Group, GroupForm} from './group.model';
import {combineLatest, forkJoin, Observable, of, throwError} from 'rxjs';
import {Person} from '../../people/state/person.model';

@Injectable({providedIn: 'root'})
export class GroupService {

  constructor(private store: GroupStore,
              private query: GroupQuery,
              private config: S4eConfig,
              private http: HttpClient) {
  }

  fetchAll(institutionSlug: string) {
    this.store.setLoading(true);
    this.store.setError(null);
    this.http.get<IPageableResponse<Institution>>(`${this.config.apiPrefixV1}/institutions/${institutionSlug}/groups`)
      .pipe(finalize(() => this.store.setLoading(false)))
      .subscribe(
        pageable => this.store.set(pageable.content),
        error => this.store.setError(error)
      );
  }

  update$(instSlug: string, groupSlug: string, value: GroupForm) {
    this.store.setLoading(true);
    return this.http.put<Group>(`${this.config.apiPrefixV1}/institutions/${instSlug}/groups/${groupSlug}`, {
      ...value,
      membersEmails: value.membersEmails._
    }).pipe(
      finalize(() => this.store.setLoading(false)),
      catchError(error => {
        this.store.setError(error);
        return throwError(error);
      })
    );
  }

  create$(instSlug: string, value: GroupForm) {
    this.store.setLoading(true);
    return this.http.post<Group>(`${this.config.apiPrefixV1}/institutions/${instSlug}/groups`, {
      ...value,
      membersEmails: value.membersEmails._
    }).pipe(
      finalize(() => this.store.setLoading(false)),
      catchError(error => {
        this.store.setError(error);
        return throwError(error);
      })
    );
  }

  fetchForm$(instSlug: string, groupSlug: string): Observable<GroupForm> {
    this.store.setLoading(true);
    return forkJoin([
      this.http.get<Group>(`${this.config.apiPrefixV1}/institutions/${instSlug}/groups/${groupSlug}`),
      this.http.get<{members: Person[]}>(`${this.config.apiPrefixV1}/institutions/${instSlug}/groups/${groupSlug}/members`)
    ]).pipe(
        finalize(() => this.store.setLoading(false)),
        catchError(e => {
          this.store.setError(e);
          return throwError(e);
        }),
      map(([group, users]) => ({
        name: group.name,
        description: '',
        membersEmails: {_: users.members.map(user => user.email)}
      })));
  }
}
