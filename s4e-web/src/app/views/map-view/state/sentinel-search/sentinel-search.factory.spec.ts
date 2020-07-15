import {SentinelFloatParam, SentinelSearchMetadata, SentinelSection} from './sentinel-search.metadata.model';
import * as Factory from 'factory.ts';
import {SentinelSearchResultResponse} from './sentinel-search.model';

export const SentinelFloatParamFactory = Factory.makeFactory<SentinelFloatParam>({
  queryParam: Factory.each(i => `floatParam${i}`),
  type: 'float',
  min: 0,
  max: 100
});

export const SentinelSectionFactory = Factory.makeFactory<SentinelSection>({
  name: Factory.each(i => `sentinel-${i}`),
  params: []
})

export const SentinelSearchMetadataFactory = Factory.makeFactory<SentinelSearchMetadata>({
  common: {
    params: [
      {
        queryParam: 'sortBy',
        type: 'select',
        values: [
          'sensingTime',
          'ingestionTime',
          'id'
        ]
      },
      {
        queryParam: 'order',
        type: 'select',
        values: [
          'DESC',
          'ASC'
        ]
      }, {
        queryParam: 'sensingFrom',
        type: 'datetime'
      }, {
        queryParam: 'sensingTo',
        type: 'datetime'
      }, {
        queryParam: 'ingestionFrom',
        type: 'datetime'
      }, {
        queryParam: 'ingestionTo',
        type: 'datetime'
      }
    ]
  },
  sections: [
    {
      name: 'sentinel-1',
      params: [
        {
          queryParam: 'satellitePlatform',
          type: 'select',
          values: [
            'Sentinel-1A',
            'Sentinel-1B'
          ]
        },
        {
          queryParam: 'productType',
          type: 'select',
          values: [
            'GRDM',
            'GRDH',
            'SLC_'
          ]
        },
        {
          queryParam: 'cloudCover',
          type: 'float',
          min: 0,
          max: 100
        },
        {
          queryParam: 'relativeOrbitNumber',
          type: 'text'
        }
      ]
    }
  ]
});

export const SentinelSearchFactory = Factory.makeFactory<SentinelSearchResultResponse>({
  id: Factory.each(i => `scene-${i}`),
  productId: Factory.each(i => i),
  timestamp: '2020-06-25T22:45:26.576Z'
});
