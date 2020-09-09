import {Legend} from '../legend/legend.model';

export interface Scene {
  id: number;
  sceneId: number;
  timestamp: string;
  legend: Legend | null;
  layerName: string;
}

export interface SceneWithUI extends Scene{
  position: number;
}

/**
 * A factory function that creates Scene
 */
export function createScene(params: Partial<Scene>) {
  return {} as Scene;
}
