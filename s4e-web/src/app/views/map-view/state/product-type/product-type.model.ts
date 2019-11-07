import {Legend} from '../legend/legend.model';

export interface ProductType {
  id: number;
  name: string;
  imageUrl: string;
  description: string;
  legend: Legend|null|undefined;
}
