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

import {Legend} from '../legend/legend.model';
import {HashMap} from '@datorama/akita';
import {BaseSceneResponse} from '../sentinel-search/sentinel-search.model';

export const SHOW_SCENE_DETAILS_QUERY_PARAM = 'show-scene-details';

export interface SceneResponse extends BaseSceneResponse {
  legend: Legend | null;
  metadataContent: HashMap<string | number>;
}

export interface Scene extends SceneResponse {
  id: number;
  sceneId: number;
  timestamp: string;
  legend: Legend | null;
  layerName: string;
}

export interface SceneWithUI extends Scene {
  position: number;
}

/**
 * A factory function that creates Scene
 */
export function createScene(params: Partial<Scene>) {
  return {} as Scene;
}
