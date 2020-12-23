/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
