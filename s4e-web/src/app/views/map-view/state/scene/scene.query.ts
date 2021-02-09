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

import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {SceneState, SceneStore} from './scene.store.service';
import {Scene, SceneWithUI} from './scene.model';
import {combineLatest, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {ProductQuery} from '../product/product.query';
import moment from 'moment';

export interface TimelineUI {
  scenes: SceneWithUI[];
  startTime: string;
}

@Injectable({
  providedIn: 'root'
})
export class SceneQuery extends QueryEntity<SceneState, Scene> {
  constructor(protected store: SceneStore, private _productQuery: ProductQuery) {
    super(store);
  }

  selectTimelineUI(): Observable<TimelineUI> {
    const SECONDS_IN_A_HOUR = 3600.0;

    return combineLatest([
      this.selectAll(),
      this.selectActive(),
      this._productQuery.selectSelectedDate(),
      this._productQuery.selectTimelineResolution()
    ]).pipe(
      map(([scenes, activeScene, date, resolution]) => {
        let startTimeline = moment(date);
        let activeSceneTime: moment.Moment = startTimeline;
        if (activeScene) {
          activeSceneTime = moment(activeScene.timestamp);
        }

        startTimeline.hours(
          Math.floor(activeSceneTime.hour() / resolution) * resolution
        );
        return {
          startTime: startTimeline.toISOString(),
          scenes: scenes
            .filter(scene => {
              const diff = moment(scene.timestamp).diff(
                startTimeline,
                'hours',
                true
              );
              return diff >= 0 && diff < resolution;
            })
            .map(scene => ({
              ...scene,
              position:
                (startTimeline.diff(scene.timestamp, 'seconds', true) /
                  (SECONDS_IN_A_HOUR * resolution)) *
                -100.0
            }))
        };
      })
    );
  }
}
