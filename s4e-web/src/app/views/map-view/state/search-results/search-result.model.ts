import {EntityState} from '@datorama/akita';

export interface SearchResult {
  id: string;
  latitude: number;
  longitude: number;
  name: string;
  type: string;
  voivodeship: string;
}

export interface SearchResultsState extends EntityState<SearchResult> {
  isOpen: boolean;
  queryString: string;
  selectedLocation: SearchResult|null;
}

export function createInitialState(): SearchResultsState {
  return {
    isOpen: false,
    queryString: '',
    selectedLocation: null
  };
}


