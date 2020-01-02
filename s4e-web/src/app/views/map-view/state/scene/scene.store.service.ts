import { Injectable } from '@angular/core';
import {ActiveState, EntityState, EntityStore, StoreConfig} from '@datorama/akita';
import { Scene } from './scene.model';

export interface SceneState extends EntityState<Scene>, ActiveState<number> {}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Scene', idKey: 'id' })
export class SceneStore extends EntityStore<SceneState, Scene> {

  constructor() {
    super();
  }
}

