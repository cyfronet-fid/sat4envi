import {HashMap} from '@datorama/akita';

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

export function createInitialState(): LegendState {
  return {
    legend: null,
    isOpen: false
  };
}
