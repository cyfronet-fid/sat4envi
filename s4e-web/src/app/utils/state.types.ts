import {SearchResult} from '../views/map-view/state/search-results/search-result.model';

export interface IPaginable<T> {
  content: T[];
  empty: boolean;
  first: boolean;
  last: boolean;
  number: number;
  numberOfElements: number;
  size: number;
  sort: string;
  totalElements: number;
  totalPages: number;
  pageable: string;
}

export interface PageSearchResult extends IPaginable<SearchResult> {}