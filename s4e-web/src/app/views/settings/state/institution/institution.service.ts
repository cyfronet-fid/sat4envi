import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {InstitutionStore} from './institution.store';
import {Institution} from './institution.model';
import { filter, flatMap, map, finalize, tap } from 'rxjs/operators';
import {InstitutionQuery} from './institution.query';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable} from 'rxjs';
import {AkitaGuidService} from 'src/app/views/map-view/state/search-results/guid.service';
import {HashMap} from '@datorama/akita';
import {toHashMap} from '../../../../utils/miscellaneous/miscellaneous';
import environment from 'src/environments/environment';
import { gt } from 'cypress/types/lodash';
import { handleHttpRequest$ } from 'src/app/common/store.util';

@Injectable({providedIn: 'root'})
export class InstitutionService {
  CONFIG: any;
  constructor(private _store: InstitutionStore,
              private _query: InstitutionQuery,
              private _router: Router,
              private _http: HttpClient,
              private _guidGenerationService: AkitaGuidService) {
  }

  findBy(slug: string): Observable<Institution | null> {
    const url = !!slug && slug !== '' && `${environment.apiPrefixV1}/institutions/${slug}` || null;
    return this._http.get<Institution>(url)
      .pipe(handleHttpRequest$(this._store));
  }

  get() {
    const url = `${environment.apiPrefixV1}/institutions`;
    this._http.get<Institution[]>(url)
      .pipe(
        handleHttpRequest$(this._store),
        tap((institutions) => this._saveIn(this._store, institutions, this._guidGenerationService))
      )
      .subscribe();
  }

  add(institution: Institution) {
    this._store.add(institution);
  }

  delete(slug: string) {
    const url = `${environment.apiPrefixV1}/institutions/${slug}`;
    return this._http.delete(url)
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => this._store.remove(slug))
      );
  }

  updateInstitution$(institution: Institution) {
    const url = `${environment.apiPrefixV1}/institutions/${institution.slug}`;
    return this._http.put<Institution>(url, institution)
      .pipe(
        handleHttpRequest$(this._store),
        tap(updatedInstitution => this._store.update(institution.slug, updatedInstitution)),
        tap(updatedInstitution => this._router.navigate(
          ['/settings/institution'],
          {
            queryParamsHandling: 'merge',
            queryParams: {
              institution: updatedInstitution.slug
            }
          }
        ))
      );
  }

  createInstitutionChild$(institution: Institution) {
    const url = `${environment.apiPrefixV1}/institutions/${institution.parentSlug}/child`;
    return this._http.post<Institution>(url, institution)
      .pipe(
        handleHttpRequest$(this._store),
        tap(newInstitution => this._store.add(newInstitution)),
        tap(newInstitution => this._router.navigate(
          ['/settings/institution'],
          {
            queryParamsHandling: 'merge',
            queryParams: {
              institution: newInstitution.slug
            }
          }
        ))
      );
  }

  setActive(slug: string) {
    this._store.setActive(slug);
  }

  connectInstitutionToQuery$(route: ActivatedRoute): Observable<string> {
    this._store.setError(null);

    const cache$ = this._query.selectHasCache().pipe(filter(cache => !!cache));
    const combinedActiveWithInstitutions$ = cache$
      .pipe(flatMap(() => combineLatest([
        this._getInstitutionsFrom$(this._query),
        this._getInstitutionSlugFrom$(route)
      ])));
    const activeInstitutionSlug$ = combinedActiveWithInstitutions$
      .pipe(
        map((params) => this._initiateActiveSlugOnEmpty(route, params)),
        filter(([institutions, activeInstitutionSlug]) => !!activeInstitutionSlug),
        map((params) => this._setActiveInstitutionSlug(this._store, params)),
        filter(activeInstitutionSlug => !!activeInstitutionSlug)
      );
    this.get();

    return activeInstitutionSlug$;
  }

  setInstitution(route: ActivatedRoute, institutionSlug: string) {
    this.setActive(institutionSlug);
    this._router
      .navigate(
        ['.'],
        {
          relativeTo: route,
          queryParamsHandling: 'merge',
          queryParams: {institution: institutionSlug}
        }
      );
  }
  protected _getInstitutionSlugFrom$(route: ActivatedRoute) {
    return route.queryParamMap
      .pipe(map(params => params.get('institution')));
  }

  protected _saveIn(store: InstitutionStore, institutions: Institution[], generator: AkitaGuidService) {
    const hash: HashMap<Institution> = toHashMap(institutions, 'slug');

    interface InstitutionWithChildren extends Institution {
      children: InstitutionWithChildren[];
    }

    function listToTree(list: Institution[]) {
      let map = {};
      let node: InstitutionWithChildren;
      let roots: InstitutionWithChildren[] = [];
      let listWithChildren: InstitutionWithChildren[] = list.map((inst, i) => {
        map[list[i].slug] = i;
        return {...list[i], children: []};
      });

      for (let i = 0; i < list.length; i += 1) {
        node = listWithChildren[i];
        if (node.parentSlug != null && !!listWithChildren[map[node.parentSlug]]) {
          // if you have dangling branches check that map[node.parentId] exists
          listWithChildren[map[node.parentSlug]].children.push(node);
        } else {
          roots.push(node);
        }
      }

      return roots;
    }

    function calculateDepths(institutions: InstitutionWithChildren[], currentDepth: number = 0): Institution[] {
      const out: Institution[] = [];

      institutions.forEach(inst => {
        inst.ancestorDepth = currentDepth;
        inst.id = generator.guid();
        const children = inst.children;
        delete inst['children'];
        out.push(inst);
        out.push(...calculateDepths(children, currentDepth+1));
      });

      return out;
    }

    store.set(calculateDepths(listToTree(institutions)));
  }

  protected _getInstitutionsFrom$(query: InstitutionQuery) {
    return query.selectAll()
      .pipe(
        filter(institutions => {
          if (institutions.length === 0) {
            this._store.setError('no_institution');
          }

          return institutions.length > 0;
        })
      );
  }

  protected _initiateActiveSlugOnEmpty(route: ActivatedRoute, params) {
    const [institutions, activeInstitutionSlug] = params;
    const institutionSlug = !!activeInstitutionSlug
      ? activeInstitutionSlug
      : institutions.shift().slug;
    if (!activeInstitutionSlug) {
      this.setInstitution(route, institutionSlug);
    }

    const slugIndex = 1;
    params[slugIndex] = institutionSlug;
    return params;
  }

  protected _setActiveInstitutionSlug(store: InstitutionStore, params) {
    const [institutions, activeInstitutionSlug] = params;
    const institutionExists = institutions
      .some(institution => institution.slug.indexOf(activeInstitutionSlug) > -1);
    if (!institutionExists) {
      store.setError('no_institution');
      return null;
    }

    store.setActive(activeInstitutionSlug);
    store.setError(null);
    return activeInstitutionSlug;
  }
}
