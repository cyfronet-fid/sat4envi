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

import {Component} from '@angular/core';
import {SceneQuery} from '../../state/scene/scene.query';
import {Scene} from '../../state/scene/scene.model';
import {ModalComponent} from '../../../../modal/utils/modal/modal.component';
import {ModalService} from '../../../../modal/state/modal.service';
import {MOBILE_MODAL_SCENE_SELECTOR_MODAL_ID} from './mobile-scene-selector-modal.model';
import {SceneService} from '../../state/scene/scene.service';

@Component({
  selector: 's4e-mobile-scene-selector-modal',
  templateUrl: './mobile-scene-selector-modal.component.html',
  styleUrls: ['./mobile-scene-selector-modal.component.scss']
})
export class MobileSceneSelectorModalComponent extends ModalComponent {
  public scenes$ = this.sceneQuery.selectAll();
  public activeSceneId$ = this.sceneQuery.selectActiveId();

  constructor(
    private sceneQuery: SceneQuery,
    private sceneService: SceneService,
    modalService: ModalService
  ) {
    super(modalService, MOBILE_MODAL_SCENE_SELECTOR_MODAL_ID);
  }

  ngOnInit() {}

  selectScene(scene: Scene) {
    this.sceneService.setActive(scene.id);
    this.dismiss();
  }
}
