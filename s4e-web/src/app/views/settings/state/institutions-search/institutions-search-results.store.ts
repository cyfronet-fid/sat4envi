import { Institution } from '../institution/institution.model';
import {Injectable} from '@angular/core';
import {StoreConfig, EntityStore} from '@datorama/akita';
import { SearchResultsState, createInitialState } from 'src/app/views/map-view/state/search-results/search-result.model';


@Injectable({providedIn: 'root'})
@StoreConfig({name: 'InstitutionSearchResults'})
export class InstitutionsSearchResultsStore extends EntityStore<SearchResultsState<Institution>, Institution> {
  constructor() {
    super(createInitialState<Institution>());
  }
}

