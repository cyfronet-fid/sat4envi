import * as Factory from 'factory.ts';
import {Scene} from './scene.model';

export const SceneFactory = Factory.makeFactory<Scene>({
  id: Factory.each(i => i),
  sceneId: Factory.each(i => i + 1000),
  timestamp: Factory.each(i => '2019-05-01T00:00:00T'),
  layerName: Factory.each(i => `layer #${i}`),
  legend: null
});

export function convertToSceneWithPosition(width: number, scenes: Scene[]) {
  return scenes.map((scene, i) => ({
    ...scene,
    position: i * 100.0 / scenes.length
  }));
}
