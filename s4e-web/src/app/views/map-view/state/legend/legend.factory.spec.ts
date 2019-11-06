import * as Factory from 'factory.ts';
import {Legend} from './legend.model';

export const LegendFactory = Factory.makeFactory<Legend>({
  type: 'gradient',
  url: Factory.each(i => `/assets/asset_${i}.svg`),
  leftDescription: {
    "0.1": "10%",
    "0.2": "20%",
    "0.3": "30%",
    "0.4": "40%",
    "0.5": "50%",
    "0.6": "60%",
    "0.7": "70%",
    "0.8": "80%",
    "0.9": "90%",
    "1.0": "100%"
  },
  rightDescription: {
    "0.1": "A",
    "0.2": "B",
    "0.3": "C",
    "0.4": "D",
    "0.5": "E",
    "0.6": "F",
    "0.7": "G",
    "0.8": "H",
    "0.9": "I",
    "1.0": "J"
  },
  topMetric: {},
  bottomMetric: {},
});
