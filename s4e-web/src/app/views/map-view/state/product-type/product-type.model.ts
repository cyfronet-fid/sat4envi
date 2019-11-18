import {Legend} from '../legend/legend.model';
import {HashMap} from '@datorama/akita';
import moment from 'moment';

export interface ProductType {
  id: number|undefined;
  name: string;
  imageUrl: string;
  description: string;
  legend: Legend|null|undefined;
}
