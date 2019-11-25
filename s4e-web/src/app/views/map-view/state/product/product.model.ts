import {Legend} from '../legend/legend.model';

export interface Product {
  id: number | undefined;
  name: string;
  imageUrl: string;
  description: string;
  legend: Legend | null | undefined;
}
