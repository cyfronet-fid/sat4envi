import {NotificationService} from 'notifications';
import {getImageWmsLoader} from '../../views/map-view/state/overlay/image-wms.utils';
import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {Overlay, OverlayUI, OwnerType, PERSONAL_OWNER_TYPE} from '../../views/map-view/state/overlay/overlay.model';
import {getImageWmsFrom} from '../../views/map-view/state/overlay/overlay.utils';
import {ModalService} from '../../modal/state/modal.service';
import {OverlayQuery} from '../../views/map-view/state/overlay/overlay.query';
import {disableEnableForm, validateAllFormFields} from '../../utils/miscellaneous/miscellaneous';
import {OverlayService} from '../../views/map-view/state/overlay/overlay.service';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {removeAutoGeneratedParams, WMS_URL_VALIDATORS} from './wms-url.utils';
import {Observable, Subject} from 'rxjs';
import Projection from 'ol/proj/Projection';
import {filter, map, switchMap, takeUntil, tap} from 'rxjs/operators';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {ActivatedRoute} from '@angular/router';

export interface OverlayForm {
  url: string;
  label: string;
}

@Component({
  selector: 's4e-overlay-list',
  templateUrl: './overlay-list.component.html',
  styleUrls: ['./overlay-list.component.scss']
})
export class OverlayListComponent implements OnInit, OnDestroy {
  public newOwner: OwnerType;
  public overlaysFilter: (overlays: (Overlay & OverlayUI)[]) => (Overlay & OverlayUI)[];
  public isAddLoading$ = this._overlayQuery.ui.select('showNewOverlayForm');
  public isAddingNewLayer$ = this._overlayQuery.ui.select('loadingNew').pipe(untilDestroyed(this));
  public isLoading$ = this._overlayQuery.selectLoading();
  public setInstitutionalFilter$ = this._activatedRoute.queryParamMap
    .pipe(
      untilDestroyed(this),
      filter(() => !!this.newOwner && this.newOwner === 'INSTITUTIONAL'),
      filter(paramsMap => paramsMap.has('institution')),
      map(paramsMap => paramsMap.get('institution')),
      tap(institutionSlug => this.overlaysFilter = overlays => overlays
        .filter(overlay => !overlay.ownerType || overlay.ownerType === 'INSTITUTIONAL')
        .filter(overlay => !!overlay.institutionSlug && overlay.institutionSlug === institutionSlug)
      )
    );
  public newOverlayForm: FormGroup<OverlayForm> = new FormGroup<OverlayForm>({
    label: new FormControl<string>('', [Validators.required]),
    url: new FormControl<string>('', [Validators.required, ...WMS_URL_VALIDATORS])
  });
  public visibilityFCList: FormControl<boolean>[] = [];
  private _overlaysChanged$: Subject<void> = new Subject();
  public overlays$: Observable<(Overlay & OverlayUI)[]> = this._overlayQuery.selectAllWithUIState()
    .pipe(
      map(overlays => this.overlaysFilter(overlays)),
      tap(overlays => {
        this._overlaysChanged$.next();
        this.visibilityFCList = overlays.map(overlay => {
          const fc = new FormControl<boolean>(overlay.visible);
          fc.valueChanges
            .pipe(takeUntil(this._overlaysChanged$))
            .subscribe(visible => this._overlayService.setVisible(overlay.id, visible));
          return fc;
        });
      })
    );

  constructor(
    private _activatedRoute: ActivatedRoute,
    private _modalService: ModalService,
    private _overlayQuery: OverlayQuery,
    private _overlayService: OverlayService,
    private _notificationService: NotificationService
  ) {
  }

  @Input()
  set newOverlayOwner(newOwner: OwnerType) {
    this.newOwner = newOwner;

    switch (newOwner) {
      case 'GLOBAL':
        this.overlaysFilter = overlays => overlays
          .filter(overlay => !overlay.ownerType || overlay.ownerType === 'GLOBAL');
        break;
      case 'PERSONAL':
        this.overlaysFilter = overlays => overlays;
        break;
    }
  }

  ngOnInit() {
    if (!this.newOwner) {
      throw Error('Owner of new overlays haven\'t been set');
    }

    this._overlayService.get().subscribe();
    this.setInstitutionalFilter$.subscribe();
    this.isAddingNewLayer$.subscribe(loadingNew => disableEnableForm(loadingNew, this.newOverlayForm));
  }

  async removeOverlay(id: number) {
    if (await this._modalService.confirm(
      'Usuń nakładkę',
      'Czy na pewno chcesz usunąć tę nakładkę? Operacja jest nieodwracalna.'
    )) {
      switch (this.newOwner) {
        case 'INSTITUTIONAL':
          this._activatedRoute.queryParamMap
            .pipe(untilDestroyed(this))
            .subscribe(params => this._overlayService
              .deleteInstitutionalOverlay(id, params.get('institution'))
            );
          break;
        case 'GLOBAL':
          this._overlayService.deleteGlobalOverlay(id);
          break;
        case 'PERSONAL':
          this._overlayService.deletePersonalOverlay(id);
          break;
      }
    }
  }

  hasErrors(controlName: string) {
    const formControl = this.newOverlayForm
      .controls[controlName] as FormControl;
    return !!formControl
      && formControl.touched
      && !!formControl.errors
      && Object.keys(formControl.errors).length > 0;
  }

  addNewOverlay() {
    validateAllFormFields(this.newOverlayForm);

    const sourceUrl = this.newOverlayForm.controls.url.value;
    const url = removeAutoGeneratedParams(sourceUrl);
    if (this.newOverlayForm.invalid) {
      return;
    }

    if (this.newOwner === 'INSTITUTIONAL') {
      this.hasLoadingError$(url)
        .pipe(
          untilDestroyed(this),
          switchMap(() => this._activatedRoute.queryParamMap),
          map(params => params.get('institution'))
        )
        .subscribe(
          institutionSlug => this._overlayService
            .createInstitutionalOverlay({...this.newOverlayForm.value, url}, institutionSlug),
          (error: string) => this._notificationService.addGeneral({content: error, type: 'error'})
        );
      return;
    }

    this.hasLoadingError$(url)
      .pipe(untilDestroyed(this))
      .subscribe(
        () => {
          switch (this.newOwner) {
            case 'PERSONAL':
              this._overlayService.createPersonalOverlay({...this.newOverlayForm.value, url});
              break;
            case 'GLOBAL':
              this._overlayService.createGlobalOverlay({...this.newOverlayForm.value, url});
              break;
          }
        },
        (error: string) => this._notificationService.addGeneral({content: error, type: 'error'})
      );
  }

  hasLoadingError$(url: string) {
    /**
     * Observable is used due to not working Open Layers Event Dispatcher
     * and force state changes
     */
    return new Observable<string | null>(observer$ => {
      const source = getImageWmsFrom({url});
      source.setImageLoadFunction(getImageWmsLoader(observer$));

      const BBOX = [49.497526,14.407200,54.573956,23.836237];
      const standardCrs = 'EPSG:3857';
      source
        .getImage(BBOX, 1, 1, new Projection({code: standardCrs}))
        .load();
    });
  }

  setNewFormVisible(show: boolean) {
    this.newOverlayForm.reset();
    this._overlayService.setNewFormVisible(show);
  }

  isPersonal(overlay: Overlay) {
    return overlay.ownerType === PERSONAL_OWNER_TYPE;
  }

  getOverlayId(overlay: Overlay): number {
    return overlay.id;
  }

  ngOnDestroy(): void {
    this._overlayService.resetUI();
  }
}
