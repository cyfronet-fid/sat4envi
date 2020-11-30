import {HashMap} from '@datorama/akita';

export const COLLAPSED_LEGEND_LOCAL_STORAGE_KEY = 'collapseLegend';

export interface Legend {
  type: 'gradient';
  url: string;
  leftDescription: HashMap<string>;
  rightDescription: HashMap<string>;
  topMetric: HashMap<string>;
  bottomMetric: HashMap<string>;
}

export interface LegendState {
  legend: Legend|null;
  isOpen: boolean;
}

