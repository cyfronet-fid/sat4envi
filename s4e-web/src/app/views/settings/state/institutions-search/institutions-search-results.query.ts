import { InstitutionService } from './../institution/institution.service';
import { map, filter, switchMap } from 'rxjs/operators';
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
  constructor(protected store: InstitutionsSearchResultsStore, protected _institutionService: InstitutionService) {
    super(store);
  }

  getSelectedInstitutionSlugBy$(activatedRoute: ActivatedRoute): Observable<string | null> {
    return activatedRoute.queryParamMap
      .pipe(
        map((params: ParamMap) => !!params.keys.filter(queryName => queryName === 'institution')
          && !!params.get('institution') && params.get('institution') || null
        )
      );
  }

  getSelectedInstitutionBy$(activatedRoute: ActivatedRoute) {
    return this.getSelectedInstitutionSlugBy$(activatedRoute)
      .pipe(
        filter((activeInstitutionSlug: string | null) => !!activeInstitutionSlug && activeInstitutionSlug !== ''),
        switchMap((activeInstitutionSlug: string) => this._institutionService.findBy(activeInstitutionSlug)),
        filter((activeInstitution: Institution | null) => !!activeInstitution)
      );
  }
}
