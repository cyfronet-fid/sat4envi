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
import {WMS_URL_VALIDATORS} from './wms-url.utils';
import {forkJoin, Observable, of, Subject} from 'rxjs';
import Projection from 'ol/proj/Projection';
import {debounceTime, filter, finalize, map, switchMap, takeUntil, tap} from 'rxjs/operators';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {ActivatedRoute} from '@angular/router';
import WMSCapabilities from 'ol/format/WMSCapabilities';
import {OverlayStore} from '../../views/map-view/state/overlay/overlay.store';

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
    private _overlayStore: OverlayStore,
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
    this._overlayStore.ui.update({loadingNew: false});
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
    this._overlayStore.ui.update({loadingNew: true});
    validateAllFormFields(this.newOverlayForm);
    if (this.newOverlayForm.invalid) {
      this._overlayStore.ui.update({loadingNew: false});
      return;
    }

    const url = this.newOverlayForm.controls.url.value.trim();
    if (this.newOwner === 'INSTITUTIONAL') {
      this.hasLoadingError$(url)
        .pipe(
          untilDestroyed(this),
          switchMap(formattedUrl => forkJoin([of(formattedUrl), this._activatedRoute.queryParamMap])),
          map(([url, params]) => [url, params.get('institution')]),
          tap(() => this._overlayStore.ui.update({loadingNew: false}))
        )
        .subscribe(
          ([url, institutionSlug]) => {
            this._overlayStore.ui.update({loadingNew: false});
            this._overlayService
              .createInstitutionalOverlay({...this.newOverlayForm.value, url}, institutionSlug);
          },
          (error: string) => {
            this._notificationService.addGeneral({content: error, type: 'error'});
            this._overlayStore.ui.update({loadingNew: false});
          }
        );
      return;
    }

    this.hasLoadingError$(url)
      .pipe(untilDestroyed(this))
      .subscribe(
        formattedUrl => {
          this._overlayStore.ui.update({loadingNew: false});
          switch (this.newOwner) {
            case 'PERSONAL':
              this._overlayService.createPersonalOverlay({...this.newOverlayForm.value, url: formattedUrl});
              break;
            case 'GLOBAL':
              this._overlayService.createGlobalOverlay({...this.newOverlayForm.value, url: formattedUrl});
              break;
          }
        },
        (error: string) => {
          this._notificationService.addGeneral({content: error, type: 'error'});
          this._overlayStore.ui.update({loadingNew: false});
        }
      );
  }

  hasLoadingError$(url: string) {
    /**
     * Observable is used due to not working Open Layers Event Dispatcher
     * and force state changes
     */
    url = url.toLowerCase().includes('request=getcapabilities') ? url.split("?")[0] : url;
    return new Observable<string | null>(observer$ => {
      const capabilitiesUrl = `${url.split('?')[0]}?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities`;
      fetch(capabilitiesUrl)
        .then(response => {
            if (response.status !== 200) {
              observer$.error(`
                Sprawdź poprawność URL i jego parametrów!

                Mógł wystąpić Cross-Origin Request Blocked:
                Polityka administracyjna serwera nie zezwala na czytanie przez źródła zewnętrzne
              `);
              observer$.complete();
              throw new Error();
            }

            return response;
          },
          error => {
            observer$.error(`
              Sprawdź poprawność URL i jego parametrów!

              Mógł wystąpić Cross-Origin Request Blocked:
              Polityka administracyjna serwera nie zezwala na czytanie przez źródła zewnętrzne
             `);
            observer$.complete();
          }
        )
        .then(response => (response as any).text(), error => {})
        .then(text => (new WMSCapabilities()).read(text), error => {})
        .then(parsedCapabilities => parsedCapabilities.Capability.Layer, error => {})
        .then(
          layerMetadata => {
            const {crs, extent, ...rest} = layerMetadata.BoundingBox[0];

            if (!url.includes('LAYERS')) {
              const layers = this._unpackLayers(layerMetadata)
                .filter(layer => !!layer.Name)
                .map(layer => layer.Name)
                .join(',');

              const hasParams = url.includes("?");

              url = hasParams ? `${url}&LAYERS=${layers}` : `${url.split('?')[0]}?LAYERS=${layers}`;
            }

            if (!url.includes('STYLES=')) {
              url = `${url}&STYLES=`
            }

            const source = getImageWmsFrom({url});
            source.setImageLoadFunction(getImageWmsLoader(observer$));
            source
              .getImage(extent, 1,1, new Projection({code: crs}))
              .load();
          }
          , error => {});
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

  private _unpackLayers(layer: any, depth = 0) {
    const MAX_DEPTH = 10;

    if (!layer) {
      return [];
    }

    if (!layer.Layer || depth > MAX_DEPTH) {
      return [layer];
    }

    return [
      layer,
      ...layer.Layer
        .map(layer => this._unpackLayers(layer, depth + 1))
        .reduce((finalLayers, layers) => finalLayers = [...finalLayers, ...layers])
    ];
  }
}
