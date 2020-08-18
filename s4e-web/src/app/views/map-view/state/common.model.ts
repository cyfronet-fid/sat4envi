import {ID} from '@datorama/akita';

export interface IUILayer {
  cid: ID;
  label: string;
  active: boolean;
  favourite: boolean;
  isLoading: boolean;
  isFavouriteLoading: boolean;
}
