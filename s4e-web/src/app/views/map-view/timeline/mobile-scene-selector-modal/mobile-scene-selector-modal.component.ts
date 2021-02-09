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
