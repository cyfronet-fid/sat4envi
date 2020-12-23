import {NotificationService} from 'notifications';
import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {Overlay, OverlayUI, OwnerType, PERSONAL_OWNER_TYPE} from '../../views/map-view/state/overlay/overlay.model';
import {ModalService} from '../../modal/state/modal.service';
import {OverlayQuery} from '../../views/map-view/state/overlay/overlay.query';
import {disableEnableForm, validateAllFormFields} from '../../utils/miscellaneous/miscellaneous';
import {OverlayService} from '../../views/map-view/state/overlay/overlay.service';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {UrlParser, OPTIONAL_WMS_URL_QUERY_PARAMS_VALIDATORS} from './wms-url.utils';
import {empty, EMPTY, Observable, Subject, throwError} from 'rxjs';
import {catchError, debounceTime, filter, map, switchMap, takeUntil, tap} from 'rxjs/operators';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {ActivatedRoute} from '@angular/router';
import {OverlayStore} from '../../views/map-view/state/overlay/overlay.store';
import {CapabilitiesMetadata, fetchCapabilitiesMetadata$, getToggledLayerInUrl, ILayer, validateImage$} from './image-wms.utils';
import * as Url from 'url';

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
    url: new FormControl<string>('', [Validators.required, ...OPTIONAL_WMS_URL_QUERY_PARAMS_VALIDATORS])
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

  public latestCapabilities: CapabilitiesMetadata;
  private _lastUrl;

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

    // metadata loading
    this.newOverlayForm.valueChanges
      .pipe(
        untilDestroyed(this),
        debounceTime(1000),
        filter(formValue => !!formValue.url && formValue.url !== ''),
        map(formValue => formValue.url.trim()),
        filter(url => {
          if (!this._lastUrl) {
            return true;
          }

          const actualUrlBase = url.split('?')[0];
          const lastUrlBase = this._lastUrl.split('?')[0];
          if (actualUrlBase !== lastUrlBase) {
            return true;
          }

          const hasLoadedLayers = !!this.latestCapabilities
            && !!this.latestCapabilities.layers;
          if (!hasLoadedLayers) {
            return true;
          }

          return false;
        }),
        map(url => {
          const urlParser = new UrlParser(url);
          const isGetCapabilitiesUrl = urlParser.has('request')
            && urlParser
              .getParamValueOf('request')
              .toLowerCase()
              .includes('GetCapabilities'.toLowerCase());
          if (isGetCapabilitiesUrl) {
            url = urlParser.getUrlBase();
            this.newOverlayForm.patchValue({url});
          }
          this._lastUrl = url;

          return url;
        }),
        tap(() => this._overlayStore.ui.update({loadingNew: true})),
        switchMap(url => fetchCapabilitiesMetadata$(url)
          .pipe(
            catchError(error => {
              this._notificationService.addGeneral({
                content: `
                Wystąpił błąd i może być on związany z Cross-Origin Request Blocked:
                Polityka administracyjna serwera nie zezwala na czytanie przez źródła zewnętrzne.

                Błąd: ${error}
            `,
                type: 'error'
              });
              this.latestCapabilities = {
                layers: [],
                crs: '',
                extent: []
              };
              this._overlayStore.ui.update({loadingNew: false});
              return EMPTY;
            })
          )
        ),
        tap(({layers, crs, extent}) => {
          const urlParser = new UrlParser(this.newOverlayForm.controls.url.value);
          if (!urlParser.has('layers')) {
            urlParser.setValues('LAYERS', ...layers.map(layer => layer.name));
            this.newOverlayForm.patchValue({url: urlParser.getFullUrl()});
          }

          this._overlayStore.ui.update({loadingNew: false});
          this.latestCapabilities = {layers, crs, extent};
        })
      )
      .subscribe();
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

  hasUrlLayer(layer: ILayer) {
    const url = this.newOverlayForm.controls.url.value;
    return !!url && url.trim().includes(layer.name);
  }

  toggleLayerInUrl(layer: ILayer) {
    const actualUrl = this.newOverlayForm.controls.url.value.trim();
    this.newOverlayForm.patchValue({url: getToggledLayerInUrl(actualUrl, layer.name)});
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

    const urlParser = new UrlParser(this.newOverlayForm.controls.url.value);
    if (!urlParser.has('layers')) {
      this._notificationService.addGeneral({
        content: 'URL powinien mieć wybraną co najmniej 1 warstwę',
        type: 'error'
      });
      this._overlayStore.ui.update({loadingNew: false});
      return;
    }

    if (
      urlParser.has('styles')
      && (
        !urlParser.getParamValueOf('styles')
        || urlParser.getParamValueOf('styles') === ''
      )
    ) {
      urlParser.remove('styles');
    }

    const {layers, crs, extent} = this.latestCapabilities;
    validateImage$(urlParser.getFullUrl(), crs, extent)
      .pipe(
        untilDestroyed(this),
        switchMap(() => this._activatedRoute.queryParamMap
          .pipe(untilDestroyed(this))
        ),
        switchMap(queryParams => {
          const label = this.newOverlayForm.controls.label.value.trim();
          const newOverlay = {label, url: urlParser.getFullUrl()};
          switch (this.newOwner) {
            case 'PERSONAL':
              return this._overlayService.createPersonalOverlay$(newOverlay);
            case 'GLOBAL':
              return this._overlayService.createGlobalOverlay$(newOverlay);
            case 'INSTITUTIONAL':
              return this._overlayService
                .createInstitutionalOverlay$(newOverlay, queryParams.get('institution'));
          }
        })
      )
      .subscribe(
        () => {},
        (error: string) => {
          this._notificationService.addGeneral({content: error, type: 'error'});
          this._overlayStore.ui.update({loadingNew: false});
        }
      );
  }

  setNewFormVisible(show: boolean) {
    this._lastUrl = null;
    this.latestCapabilities = null;
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
