/*
 * Copyright 2021 ACC Cyfronet AGH
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

import {
  SentinelFloatParam,
  SentinelSearchMetadata,
  SentinelSection
} from './sentinel-search.metadata.model';
import * as Factory from 'factory.ts';
import {RecPartial} from 'factory.ts';
import {
  createSentinelSearchResult,
  SentinelSearchResultResponse
} from './sentinel-search.model';

export const SentinelFloatParamFactory = Factory.makeFactory<SentinelFloatParam>({
  queryParam: Factory.each(i => `floatParam${i}`),
  label: Factory.each(i => `floatParam Name #${i}`),
  type: 'float',
  min: 0,
  max: 100
});

export const SentinelSectionFactory = Factory.makeFactory<SentinelSection>({
  name: Factory.each(i => `sentinel-${i}`),
  label: Factory.each(i => `sentinel ${i}`),
  params: []
});

export const SentinelSearchMetadataFactory = Factory.makeFactory<SentinelSearchMetadata>(
  {
    common: {
      params: [
        {
          label: 'Sortuj po',
          queryParam: 'sortBy',
          type: 'select',
          values: ['sensingTime', 'ingestionTime', 'id']
        },
        {
          label: 'Kolejnosć',
          queryParam: 'order',
          type: 'select',
          values: ['DESC', 'ASC']
        },
        {
          label: 'Od',
          queryParam: 'sensingFrom',
          type: 'datetime'
        },
        {
          label: 'Do',
          queryParam: 'sensingTo',
          type: 'datetime'
        },
        {
          label: 'Pobranie Od',
          queryParam: 'ingestionFrom',
          type: 'datetime'
        },
        {
          label: 'Pobranie Do',
          queryParam: 'ingestionTo',
          type: 'datetime'
        }
      ]
    },
    sections: [
      {
        label: 'Sentinel 1',
        name: 'sentinel-1',
        params: [
          {
            label: 'Platforma',
            queryParam: 'satellitePlatform',
            type: 'select',
            values: [
              {value: 'Sentinel-1A', label: 'Sentinel-1A'},
              {value: 'Sentinel-1B', label: 'Sentinel-1B'}
            ]
          },
          {
            label: 'Typ',
            queryParam: 'productType',
            type: 'select',
            values: [
              {value: 'GRDM', label: 'GRDM'},
              {value: 'GRDH', label: 'GRDH'},
              {value: 'SLC_', label: 'SLC_'}
            ]
          },
          {
            label: 'Pokrycie Chmur',
            queryParam: 'cloudCover',
            type: 'float',
            min: 0,
            max: 100
          },
          {
            label: 'Numer Orbity',
            queryParam: 'relativeOrbitNumber',
            type: 'text'
          }
        ]
      }
    ]
  }
);

class _SentinelSearchFactory extends Factory.Factory<SentinelSearchResultResponse> {
  constructor() {
    super(
      {
        id: Factory.each(i => i),
        productId: Factory.each(i => i),
        timestamp: '2020-06-25T22:45:26.576Z',
        footprint:
          'POLYGON((20.602945 46.652897,17.222746 47.056656,17.563738 48.554222,21.041603 48.14983,20.602945 46.652897))',
        artifacts: [
          'thumbnail',
          'metadata',
          'manifest',
          'checksum',
          'RGBs_8b',
          'RGB_16b',
          'quicklook',
          'product_archive'
        ],
        sceneKey:
          'Sentinel-1/SLC_/2020-02-01/S1A_IW_SLC__1SDV_20200201T042809_20200201T042837_031054_039147_B63E.scene',
        metadataContent: {
          format: 'GeoTiff',
          ingestion_time: '2020-02-28T05:11:42.000000+00:00',
          polarisation: 'Dual VV/VH',
          polygon:
            '46.652897,20.602945 47.056656,17.222746 48.554222,17.563738 48.149830,21.041603',
          processing_level: '1',
          product_type: 'GRDH',
          relative_orbit_number: '51',
          schema: 'Sentinel-1.metadata.v1.json',
          sensing_time: '2020-02-28T04:53:47.147051+00:00',
          sensor_mode: 'IW',
          spacecraft: 'Sentinel-1A'
        },
        hasZipArtifact: true
      },
      undefined
    );
  }

  buildListSearchResult(
    count: number,
    item?: RecPartial<SentinelSearchResultResponse>
  ) {
    return this.buildList(count, item).map(item => createSentinelSearchResult(item));
  }

  buildSearchResult(item?: RecPartial<SentinelSearchResultResponse>) {
    return createSentinelSearchResult(this.build(item));
  }
}

export const SentinelSearchFactory = new _SentinelSearchFactory();
