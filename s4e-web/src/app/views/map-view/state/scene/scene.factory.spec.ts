import * as Factory from 'factory.ts';
import {Scene} from './scene.model';

export const SceneFactory = Factory.makeFactory<Scene>({
  id: Factory.each(i => i),
  sceneId: Factory.each(i => i + 1000),
  timestamp: Factory.each(i => '2019-05-01T00:00:00T'),
  layerName: Factory.each(i => `layer #${i}`),
  legend: null
});
