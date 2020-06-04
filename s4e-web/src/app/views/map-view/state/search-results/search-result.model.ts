import {EntityState} from '@datorama/akita';

export interface SearchResultsState<T> extends EntityState<T> {
  isOpen: boolean;
  queryString: string;
  searchResult: T | null;
}

export function createInitialState<T>(): SearchResultsState<T> {
  return {
    isOpen: false,
    queryString: '',
    searchResult: null
  };
}


