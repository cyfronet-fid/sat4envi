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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {DataPoint, PointStacker, TimelineComponent} from './timeline.component';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MapModule} from '../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {LocalStorageTestingProvider, RemoteConfigurationTestingProvider} from '../../../app.configuration.spec';
import {SimpleChange} from '@angular/core';
import {Scene, SceneWithUI} from '../state/scene/scene.model';
import {convertToSceneWithPosition, SceneFactory} from '../state/scene/scene.factory.spec';
import {take, toArray} from 'rxjs/operators';

describe('PointStacker', () => {
  let stacker: PointStacker = new PointStacker(10, 15);;
  const totalSceneCount = 4;
  let scenes: Scene[];
  let scenesWithPositions: SceneWithUI[];

  beforeEach(() => {
    scenes = SceneFactory.buildList(totalSceneCount);
  });

  it('should stack', () => {
    const width = 10;
    scenesWithPositions = convertToSceneWithPosition(width, scenes)
    let output = stacker.stack(scenesWithPositions, width, null);
    expect(output).toEqual([{points: scenesWithPositions, position: 37.5, selected: false}]);
  });

  it('should partially stack', () => {
    const width = 100;
    scenesWithPositions = convertToSceneWithPosition(width, scenes);

    let output = stacker.stack(scenesWithPositions, width, null);
    expect(output).toEqual([
      {points: [scenesWithPositions[0], scenesWithPositions[1]], position: 12.5, selected: false},
      {points: [scenesWithPositions[2], scenesWithPositions[3]], position: 62.5, selected: false},
    ]);
  });

  it('should flag selected', () => {
    const width = 100;
    scenesWithPositions = convertToSceneWithPosition(width, scenes);

    let output = stacker.stack(scenesWithPositions, width, scenesWithPositions[2]);
    expect(output).toEqual([
      {points: [scenesWithPositions[0], scenesWithPositions[1]], position: 12.5, selected: false},
      {points: [scenesWithPositions[2], scenesWithPositions[3]], position: 62.5, selected: true},
    ]);
  });

  it('should not stack', () => {
    const width = 200;
    scenesWithPositions = convertToSceneWithPosition(width, scenes);

    let output = stacker.stack(scenesWithPositions, width, null);
    expect(output).toEqual([
      {points: [scenesWithPositions[0]], position: scenesWithPositions[0].position, selected: false},
      {points: [scenesWithPositions[1]], position: scenesWithPositions[1].position, selected: false},
      {points: [scenesWithPositions[2]], position: scenesWithPositions[2].position, selected: false},
      {points: [scenesWithPositions[3]], position: scenesWithPositions[3].position, selected: false},
    ]);
  });
});

describe('TimelineComponent', () => {
  let component: TimelineComponent;
  let fixture: ComponentFixture<TimelineComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MapModule,
        NoopAnimationsModule,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        LocalStorageTestingProvider,
        RemoteConfigurationTestingProvider
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TimelineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should calculate hourmarks', () => {
    component.resolution = 12;
    component.startTime = '2020-09-09';
    component.ngOnChanges({
      startTime: new SimpleChange(null, component.startTime, false),
      resolution: new SimpleChange(24, component.resolution, false)
    });
    expect(component.hourmarks).toEqual(
      [
        '00:00',
        '02:00',
        '04:00',
        '06:00',
        '08:00',
        '10:00']);
  });

  it('should stackPoint', async () => {
    const width = 200;
    const scenes = convertToSceneWithPosition(width, SceneFactory.buildList(4));
    component._activeScene = null;
    component.scenesWithUI = scenes;
    jest.spyOn((component as any).element.nativeElement, 'clientWidth', 'get').mockReturnValue(width);
    component.onResize();

    expect(await component.scenes$.pipe(take(2), toArray()).toPromise()).toEqual([
      [],
      scenes.map(scene => ({points: [scene], selected: false, position: scene.position} as DataPoint))
    ]);
  });

});
