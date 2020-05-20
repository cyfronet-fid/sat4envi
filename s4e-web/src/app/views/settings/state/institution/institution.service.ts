import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {InstitutionStore} from './institution.store';
import {Institution} from './institution.model';
import {filter, finalize, flatMap, map, delay, shareReplay} from 'rxjs/operators';
import {InstitutionQuery} from './institution.query';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable} from 'rxjs';
import { AkitaGuidService } from 'src/app/views/map-view/state/search-results/guid.service';
import { S4eConfig } from 'src/app/utils/initializer/config.service';
import { IPageableResponse } from 'src/app/state/pagable.model';
import { catchErrorAndHandleStore } from 'src/app/common/store.util';

@Injectable({providedIn: 'root'})
export class InstitutionService {
  CONFIG: any;
  constructor(private store: InstitutionStore,
              private s4EConfig: S4eConfig,
              private query: InstitutionQuery,
              private router: Router,
              private http: HttpClient,
              private guidGenerationService: AkitaGuidService) {
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
      .subscribe(institutions => this.store
        .set(institutions
          .map(institution => ({
            ...institution,
            id: this.guidGenerationService.guid()
          }))
        )
      );
  }

  add(institution: Institution) {
    this.store.add(institution);
  }

  addInstitutionChild$(institution: Institution) {
    this.store.setLoading(true);
    const request = this.http
      .post<Institution>(
        `${this.s4EConfig.apiPrefixV1}/institutions/${institution.parentInstitutionSlug}/child`,
        institution
      )
      .pipe(
        map(() => true),
        catchErrorAndHandleStore(this.store),
        finalize(() => this.store.setLoading(false)),
        shareReplay(1)
      );
    return request.subscribe();
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
