import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PersonStore} from './person.store';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {Observable} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {DEFAULT_GROUP_SLUG, Person, PersonForm} from './person.model';
import {catchErrorAndHandleStore} from '../../../../common/store.util';

@Injectable({providedIn: 'root'})
export class PersonService {

  constructor(private store: PersonStore,
              private http: HttpClient, private config: S4eConfig) {
  }

  fetchAll(institutionSlug: string) {
    this.store.setLoading(true);
    this.store.setError(null);
    this.http.get<{ members: Person[] }>(`${this.config.apiPrefixV1}/institutions/${institutionSlug}/groups/${DEFAULT_GROUP_SLUG}/members`)
      .pipe(
        finalize(() => this.store.setLoading(false)),
        catchErrorAndHandleStore(this.store)
      )
      .subscribe(data => this.store.set(data.members));
  }

  create$(institutionSlug: string, value: PersonForm): Observable<Person> {
    this.store.setLoading(true);

    return this.http.post<Person>(
      `${this.config.apiPrefixV1}/institutions/${institutionSlug}/users`,
      {...value, groupSlugs: [...value.groupSlugs._, 'default']}
    ).pipe(
      catchErrorAndHandleStore(this.store),
      finalize(() => this.store.setLoading(false))
    );
  }
}
