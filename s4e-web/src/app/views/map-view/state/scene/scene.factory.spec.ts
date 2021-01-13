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

import * as Factory from 'factory.ts';
import {Scene, SceneResponse} from './scene.model';

export const SceneResponseFactory = Factory.makeFactory<SceneResponse>({
  artifacts: ['product_file', 'original_file', 'raw_data', 'metadata'],
  id: 351,
  legend: null,
  sceneKey: 'MSG_Products_WM/NatColor/20200201/202002010030_RGB_Nat_Col.scene',
  timestamp: Factory.each(i => '2019-05-01T00:00:00T'),
  metadataContent: {
    format: 'COG',
    polygon: '58.9,-52.7 73.4,-22.0 75.5,22.9 71.5,48 66.8,57.3 59.3,64.5 47.8,70.0 38.3,53.9 23.4,40.9 31.0,15.0 26.8,-13.7 40.9,-22.7 51.6,-35 58.9,-52.7',
    processing_level: '2',
    product_type: 'NatCol',
    schema: 'MSG.metadata.v1.json',
    sensing_time: '2020-02-01T00:30:00.0+00:00',
    sensor_mode: 'full_scan',
    spacecraft: 'MSG',
    productId: 7
  }
});

export const SceneFactory = Factory.makeFactory<Scene>({
  id: Factory.each(i => i),
  sceneId: Factory.each(i => i + 1000),
  timestamp: Factory.each(i => '2019-05-01T00:00:00T'),
  layerName: Factory.each(i => `layer #${i}`),
  legend: null,
  artifacts: ['product_file', 'original_file', 'raw_data', 'metadata'],
  sceneKey: 'MSG_Products_WM/NatColor/20200201/202002010030_RGB_Nat_Col.scene',
  metadataContent: {
    format: 'COG',
    polygon: '58.9,-52.7 73.4,-22.0 75.5,22.9 71.5,48 66.8,57.3 59.3,64.5 47.8,70.0 38.3,53.9 23.4,40.9 31.0,15.0 26.8,-13.7 40.9,-22.7 51.6,-35 58.9,-52.7',
    processing_level: '2',
    product_type: 'NatCol',
    schema: 'MSG.metadata.v1.json',
    sensing_time: '2020-02-01T00:30:00.0+00:00',
    sensor_mode: 'full_scan',
    spacecraft: 'MSG',
    productId: 7
  }
});

export function convertToSceneWithPosition(width: number, scenes: Scene[]) {
  return scenes.map((scene, i) => ({
    ...scene,
    position: i * 100.0 / scenes.length
  }));
}
