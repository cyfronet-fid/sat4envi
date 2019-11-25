import {Legend} from '../legend/legend.model';

export interface Scene {
  id: number;
  sceneId: number;
  timestamp: string;
  layerName: string;
  legend: Legend | null;
}

/**
 * A factory function that creates Scene
 */
export function createScene(params: Partial<Scene>) {
  return {} as Scene;
}
