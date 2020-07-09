import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {InstitutionStore} from './institution.store';
import {Institution} from './institution.model';
import {filter, flatMap, map} from 'rxjs/operators';
import {InstitutionQuery} from './institution.query';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable} from 'rxjs';
import {AkitaGuidService} from 'src/app/views/map-view/state/search-results/guid.service';
import {S4eConfig} from 'src/app/utils/initializer/config.service';
import {httpDeleteRequest$, httpGetRequest$, httpPostRequest$, httpPutRequest$} from 'src/app/common/store.util';
import {HashMap} from '@datorama/akita';
import {toHashMap} from '../../../../utils/miscellaneous/miscellaneous';

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

  findBy(slug: string): Observable<Institution | null> {
    const url = !!slug && slug !== '' && `${this.s4EConfig.apiPrefixV1}/institutions/${slug}` || null;
    return httpGetRequest$<Institution>(this.http, url, this.store);
  }

  get() {
    if (this.query.getHasCache()) {
      return;
    }
    const url = `${this.s4EConfig.apiPrefixV1}/institutions`;
    httpGetRequest$<Institution[]>(this.http, url, this.store)
      .subscribe(institutions => this._saveIn(this.store, institutions, this.guidGenerationService));
  }

  add(institution: Institution) {
    this.store.add(institution);
  }

  updateInstitution$(institution: Institution) {
    const url = `${this.s4EConfig.apiPrefixV1}/institutions/${institution.slug}`;
    return  httpPutRequest$(this.http, url, institution, this.store).subscribe(() => this.get());
  }

  createInstitutionChild$(institution: Institution) {
    const url = `${this.s4EConfig.apiPrefixV1}/institutions/${institution.parentSlug}/child`;
    return  httpPostRequest$(this.http, url, institution, this.store).subscribe(() => this.get());
  }

  setActive(slug: string) {
    this.store.setActive(slug);
  }

  connectInstitutionToQuery$(route: ActivatedRoute): Observable<string> {
    this.store.setError(null);

    const cache$ = this.query.selectHasCache().pipe(filter(cache => !!cache));
    const combinedActiveWithInstitutions$ = cache$
      .pipe(flatMap(() => combineLatest([
        this._getInstitutionsFrom$(this.query),
        this._getInstitutionSlugFrom$(route)
      ])));
    const activeInstitutionSlug$ = combinedActiveWithInstitutions$
      .pipe(
        map((params) => this._initiateActiveSlugOnEmpty(route, params)),
        filter(([institutions, activeInstitutionSlug]) => !!activeInstitutionSlug),
        map((params) => this._setActiveInstitutionSlug(this.store, params)),
        filter(activeInstitutionSlug => !!activeInstitutionSlug)
      );
    this.get();

    return activeInstitutionSlug$;
  }

  setInstitution(route: ActivatedRoute, institutionSlug: string) {
    this.setActive(institutionSlug);
    this.router
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
        if (node.parentSlug != null) {
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
            this.store.setError('no_institution');
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

  delete(slug: string) {
    return httpDeleteRequest$(this.http, `${this.s4EConfig.apiPrefixV1}/institutions/${slug}`, this.store)
      .subscribe(() => this.store.remove(slug));
  }
}
