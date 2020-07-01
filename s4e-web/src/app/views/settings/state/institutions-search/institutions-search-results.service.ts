import {InstitutionQuery} from './../institution/institution.query';
import {InstitutionService} from './../institution/institution.service';
import {InstitutionsSearchResultsStore} from './institutions-search-results.store';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {AkitaGuidService} from 'src/app/views/map-view/state/search-results/guid.service';
import {Institution} from '../institution/institution.model';

@Injectable({providedIn: 'root'})
export class InstitutionsSearchResultsService {

  constructor(private _store: InstitutionsSearchResultsStore,
              private _guidGenerationService: AkitaGuidService,
              private _insitutionService: InstitutionService,
              private _InstitutionQuery: InstitutionQuery,
              private _http: HttpClient,
              private s4EConfig: S4eConfig) {
  }

  get(partialInstitutionName: string) {
    this._store.update({isOpen: true, queryString: partialInstitutionName});

    this._store.setLoading(true);
    this._InstitutionQuery.selectAll()
      .subscribe(institutions => this._store.set(
        institutions.filter((institution) => institution.name
          .toLocaleLowerCase()
          .indexOf(partialInstitutionName.toLocaleLowerCase()) > -1
        )
        )
      );
  }

  setSelectedInstitution(searchResult: Institution | null) {
    this._store.update({searchResult: searchResult, isOpen: false});

    if (!!searchResult) {
      this._insitutionService.setActive(searchResult.slug);
    }
  }
}
