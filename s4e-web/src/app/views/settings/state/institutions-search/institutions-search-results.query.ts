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

  addChild$(activatedRoute: ActivatedRoute) {
    return this._routeToParam$(activatedRoute, 'addChild');
  }

  getInstitutionSlugFrom$(activatedRoute: ActivatedRoute) {
    return this._routeToParam$(activatedRoute, 'institution');
  }

  hasInstitutionSlugIn$(activatedRoute: ActivatedRoute) {
    return this.getInstitutionSlugFrom$(activatedRoute)
      .pipe(map(slug => !!slug));
  }

  getInstitutionFrom$(activatedRoute: ActivatedRoute): Observable<Institution | null> {
    return this._slugToInstitution$(this.getInstitutionSlugFrom$(activatedRoute));
  }

  protected _slugToInstitution$(slug$) {
    return slug$
      .pipe(
        filter((institutionSlug: string | null) => !!institutionSlug && institutionSlug !== ''),
        switchMap((institutionSlug: string) => this._institutionService.findBy(institutionSlug)),
        filter((institution: Institution | null) => !!institution)
      );
  }
  protected _routeToParam$(activatedRoute: ActivatedRoute, name: string): Observable<string | null> {
    return activatedRoute.queryParamMap
      .pipe(map((params: ParamMap) => params.has(name) && params.get(name) || null));
  }
}
