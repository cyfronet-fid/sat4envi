import { map } from 'rxjs/operators';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { InstitutionsSearchResultsStore } from './institutions-search-results.store';
import { Institution } from '../institution/institution.model';
import {Injectable} from '@angular/core';
import { SearchResultsQuery } from 'src/app/views/map-view/state/search-results/search-results.query';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class InstitutionsSearchResultsQuery extends SearchResultsQuery<Institution> {
  constructor(protected store: InstitutionsSearchResultsStore) {
    super(store);
  }

  hasSelectedInstitutionBy$(activatedRoute: ActivatedRoute): Observable<boolean> {
    return activatedRoute.queryParamMap
      .pipe(
        map((params: ParamMap) => !!params.keys.filter(queryName => queryName === 'institution')
          && !!params.get('institution')
        )
      );
  }
}
