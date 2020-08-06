import { InstitutionService } from './../institution/institution.service';
import { map, filter, switchMap, shareReplay } from 'rxjs/operators';
import { ActivatedRoute, ParamMap, Router, RoutesRecognized, convertToParamMap } from '@angular/router';
import { InstitutionsSearchResultsStore } from './institutions-search-results.store';
import { Institution } from '../institution/institution.model';
import {Injectable} from '@angular/core';
import { SearchResultsQuery } from 'src/app/views/map-view/state/search-results/search-results.query';
import { Observable, of } from 'rxjs';

export const INSTITUTION_QUERY_PARAM = 'institution';
export const ADD_CHILD_QUERY_PARAM = 'add_child';

@Injectable({
  providedIn: 'root'
})
export class InstitutionsSearchResultsQuery extends SearchResultsQuery<Institution> {

  constructor(
    protected _store: InstitutionsSearchResultsStore,
    protected _institutionService: InstitutionService
  ) {
    super(_store);
  }

  isChildAddition$(activatedRoute: ActivatedRoute) {
    return activatedRoute.queryParamMap.pipe(map((params) => params.has(ADD_CHILD_QUERY_PARAM)));
  }

  isAnyInstitutionActive$(activatedRoute: ActivatedRoute) {
    return activatedRoute.queryParamMap.pipe(map((params => params.has(INSTITUTION_QUERY_PARAM))));
  }

  selectActive$(activatedRoute: ActivatedRoute): Observable<Institution> {
    return activatedRoute.queryParamMap
      .pipe(
        filter(params => params.has(INSTITUTION_QUERY_PARAM) && params.get(INSTITUTION_QUERY_PARAM) !== ''),
        map((params) => params.get(INSTITUTION_QUERY_PARAM)),
        switchMap((institutionSlug: string) => this._institutionService.findBy(institutionSlug)),
        filter((institution: Institution | null) => !!institution),
        shareReplay(1)
      );
  }
}
