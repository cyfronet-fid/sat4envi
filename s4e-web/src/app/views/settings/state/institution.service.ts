import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {InstitutionStore} from './institution.store';
import {Institution} from './institution.model';
import {S4eConfig} from '../../../utils/initializer/config.service';
import {filter, finalize, flatMap, map} from 'rxjs/operators';
import {InstitutionQuery} from './institution.query';
import {IPageableResponse} from '../../../state/pagable.model';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable} from 'rxjs';
import {catchErrorAndHandleStore} from '../../../common/store.util';

@Injectable({providedIn: 'root'})
export class InstitutionService {
  constructor(private store: InstitutionStore,
              private s4EConfig: S4eConfig,
              private query: InstitutionQuery,
              private router: Router,
              private http: HttpClient) {
  }

  get() {
    if (this.query.getHasCache()) {
      return;
    }

    this.store.setLoading(true);
    this.store.setError(null);
    this.http.get<Institution[]>(`${this.s4EConfig.apiPrefixV1}/institutions`)
      .pipe(
        catchErrorAndHandleStore(this.store),
        finalize(() => this.store.setLoading(false))
      )
      .subscribe(institutions => this.store.set(institutions));
  }

  add(institution: Institution) {
    this.store.add(institution);
  }

  setInstitution(route: ActivatedRoute, institutionSlug: string) {
    this.setActive(institutionSlug);
    this.router.navigate(['.'], {relativeTo: route, queryParamsHandling: 'merge', queryParams: {institution: institutionSlug}});
  }

  setActive(slug: string) {
    this.store.setActive(slug);
  }

  connectInstitutionToQuery$(route: ActivatedRoute): Observable<string> {
    this.store.setError(null);

    const r = this.query.selectHasCache().pipe(
      filter(cache => cache),
      flatMap(() => combineLatest([
        this.query.selectAll(),
        route.queryParamMap.pipe(map(params => params.get('institution')))
      ])),
      map(([institutions, selectedInstitution]) => {
        const error = null;
        if (selectedInstitution == null) {
          if (institutions.length == 0) {
            this.store.setError('no_institution');
            return null;
          }
          // if the user has access to this view he/she MUST be able to see at least one institution
          this.setInstitution(route, institutions[0].slug);
          this.store.setError(null);
          return institutions[0].slug;
        } else {
          const selectedInst = institutions.find(i => i.slug == selectedInstitution);
          if (selectedInst == null) {
            this.store.setError('no_institution');
            return null;
          }
          this.store.setActive(selectedInst.slug);
          this.store.setError(null);
          return selectedInst.slug;
        }
      }),
      filter(slug => slug != null)
    );

    this.get();

    return r;
  }
}
