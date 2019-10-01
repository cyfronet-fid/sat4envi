export interface Legend {
  type: 'gradient';
  url: string;
  leftDescription: { [key: number]: string };
  rightDescription: { [key: number]: string };
  metricTop: string;
  metricBottom: string;
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
