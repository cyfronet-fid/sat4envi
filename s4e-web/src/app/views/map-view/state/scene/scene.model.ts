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
