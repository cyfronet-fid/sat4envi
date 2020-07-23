import {Component, OnDestroy, OnInit} from '@angular/core';
import {ModalComponent} from '../../../../modal/utils/modal/modal.component';
import {ModalService} from '../../../../modal/state/modal.service';
import {OVERLAY_LIST_MODAL_ID} from './overlay-list-modal.model';
import {OverlayQuery} from '../../state/overlay/overlay.query';
import {Observable, Subject} from 'rxjs';
import {Overlay, OverlayUI} from '../../state/overlay/overlay.model';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {map, takeUntil} from 'rxjs/operators';
import {OverlayService} from '../../state/overlay/overlay.service';
import {disableEnableForm, validateAllFormFields} from '../../../../utils/miscellaneous/miscellaneous';
import {untilDestroyed} from 'ngx-take-until-destroy';

export interface OverlayForm {
  url: string;
  layer: string;
  layerName: string;
}

@Component({
  selector: 's4e-overlay-list-modal',
  templateUrl: './overlay-list-modal.component.html',
  styleUrls: ['./overlay-list-modal.component.scss']
})
export class OverlayListModalComponent extends ModalComponent implements OnInit, OnDestroy {
  public loading$: Observable<boolean>;
  public overlays$: Observable<(Overlay & OverlayUI)[]>;
  public visibilityFCList: FormControl<boolean>[] = [];
  public showOverlayForm$: Observable<boolean>;
  public addingNewLayer$: Observable<boolean>;
  public hasCustomOverlays$: Observable<boolean>;
  private _overlaysChanged$: Subject<void> = new Subject();
  public newOverlayForm: FormGroup<OverlayForm> = new FormGroup<OverlayForm>({
    layerName: new FormControl<string>('', [Validators.required]),
    url: new FormControl<string>('', [Validators.required]),
    layer: new FormControl<string>('', [Validators.required])
  });

  constructor(modalService: ModalService, private _overlayQuery: OverlayQuery, private _overlayService: OverlayService) {
    super(modalService, OVERLAY_LIST_MODAL_ID);
  }

  ngOnInit() {
    this.loading$ = this._overlayQuery.selectLoading();
    this.overlays$ = this._overlayQuery.selectAllWithUIState();
    this.hasCustomOverlays$ = this.overlays$.pipe(map(overlays => overlays.find(ol => ol.mine) !== undefined));
    this.showOverlayForm$ = this._overlayQuery.ui.select('showNewOverlayForm');
    this.overlays$.subscribe(overlays => {
      this._overlaysChanged$.next();
      this.visibilityFCList = overlays.map(overlay => {
        const fc = new FormControl<boolean>(overlay.visible);
        fc.valueChanges.pipe(takeUntil(this._overlaysChanged$)).subscribe(visible => this._overlayService.setVisible(overlay.id, visible))
        return fc
      })
    });

    this.addingNewLayer$ = this._overlayQuery.ui.select('loadingNew').pipe(untilDestroyed(this));
    this.addingNewLayer$.subscribe(loadingNew => disableEnableForm(loadingNew, this.newOverlayForm));
  }

  ngOnDestroy(): void {
    this._overlayService.resetUI();
  }

  async removeOverlay(id: string) {
    if(await this.modalService.confirm('Usuń nakładkę', 'Czy na pewno chcesz usunąć tę nakładkę? Operacja jest nieodwracalna.')) {
      this._overlayService.deleteOverlay(id);
    }
  }

  showOverlayForm(show: boolean) {
    this.newOverlayForm.reset();
    this._overlayService.setNewFormVisible(show);
  }

  addNewOverlay() {
    validateAllFormFields(this.newOverlayForm);

    if (this.newOverlayForm.invalid) {
      return;
    }

    this._overlayService.createOverlay(
      this.newOverlayForm.value.layerName,
      this.newOverlayForm.value.url,
      this.newOverlayForm.value.layer);
  }

  getOverlayId(overlay: Overlay): string {
    return overlay.id
  }
}
