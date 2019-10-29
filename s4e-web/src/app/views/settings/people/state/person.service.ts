import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PersonStore} from './person.store';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {finalize} from 'rxjs/operators';
import {DEFAULT_GROUP_NAME, Person, PersonForm} from './person.model';
import {Observable, of} from 'rxjs';

@Injectable({providedIn: 'root'})
export class PersonService {

  constructor(private store: PersonStore,
              private http: HttpClient, private config: S4eConfig) {
  }

  fetchAll(institutionSlug: string) {
    this.store.setLoading(true);
    this.store.setError(null);
    this.http.get<{ members: Person[] }>(`${this.config.apiPrefixV1}/institutions/${institutionSlug}/groups/${DEFAULT_GROUP_NAME}/members`)
      .pipe(finalize(() => this.store.setLoading(false)))
      .subscribe(
        data => this.store.set(data.members),
        error => this.store.setError(error)
      );
  }

  create$(institutionSlug: string, value: PersonForm): Observable<Person> {
    console.log('saving...', institutionSlug, value);
    return of(null)
  }
}
