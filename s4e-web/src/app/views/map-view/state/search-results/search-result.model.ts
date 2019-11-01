export interface SearchResult {
  id: string;
  latitude: number;
  longitude: number;
  name: string;
  type: string;
  voivodeship: string;
}

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

