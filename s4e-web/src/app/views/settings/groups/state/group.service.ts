import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {GroupStore} from './group.store';
import {IPageableResponse} from '../../../../state/pagable.model';
import {Institution} from '../../state/institution.model';
import {finalize} from 'rxjs/operators';
import {GroupQuery} from './group.query';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {GroupForm} from './group.model';
import {of} from 'rxjs';

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

  create$(instSlug: string, value: GroupForm) {
    console.log('create$ group', value);
    return of(true);
  }
}
