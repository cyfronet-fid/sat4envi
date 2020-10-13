import { InjectorModule } from 'src/app/common/injector.module';
import {ImageWMS} from 'ol/source';
import {Image, Layer} from 'ol/layer';
import {IUILayer} from '../common.model';
import {EntityState} from '@datorama/akita';
import { getBaseUrlAndParamsFrom } from '../../view-manager/overlay-list-modal/wms-url.utils';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { NotificationService } from 'notifications';
import { ImageWmsLoader } from '../utils/layers-loader.util';

// Overlay
// IUILayer -> caption to label

export const GLOBAL_OWNER_TYPE = 'GLOBAL';
export const INSTITUTIONAL_OWNER_TYPE = 'INSTITUTIONAL';
export const PERSONAL_OWNER_TYPE = 'PERSONAL';
export type OwnerType = typeof GLOBAL_OWNER_TYPE | typeof PERSONAL_OWNER_TYPE | typeof INSTITUTIONAL_OWNER_TYPE;

export interface Overlay {
  id: string;
  ownerType: OwnerType;
  url: string;
  label: string;
  layerName: string;
  visible: boolean;
  createdAt: string|null;
}

/**
 * This is transient Overlay object which can be returned by some
 * queries
 */
export interface UIOverlay extends Overlay, IUILayer {
  olLayer: Layer;
}

export function convertToUIOverlay(
  overlay: Overlay,
  active: boolean = false
): UIOverlay {
  // TODO: Remove layers name param from overlay response
  // Merge into PRG overlays dynamic url layers param with it's values
  // Remove layers name from DB
  const urlAndParams = getBaseUrlAndParamsFrom(overlay.url);
  const {url, ...urlParams} = !!urlAndParams && urlAndParams || {url: overlay.url};
  const source = new ImageWMS({
    crossOrigin: 'Anonymous',
    serverType: 'geoserver',
    url,
    params: { LAYERS: overlay.layerName, ...urlParams}
  });
  handleLoadingOf(source);
  return {
    favourite: false,
    ...overlay,
    olLayer: new Image({ source }),
    cid: overlay.id,
    active: active,
    isLoading: false,
    isFavouriteLoading: false
  };
}

function handleLoadingOf(source: ImageWMS) {
  const loaderService = InjectorModule.Injector.get(NgxUiLoaderService);
  const imageWmsLoader = new ImageWmsLoader(source);
  imageWmsLoader.start$
    .then(
      () => loaderService.startBackground(),
      () => handleLoadError(loaderService)
    );
  imageWmsLoader.end$
    .then(
      () => loaderService.stopBackground(),
      () => handleLoadError(loaderService)
    );
}

function handleLoadError(loaderService: NgxUiLoaderService) {
  loaderService.stopBackground();
  InjectorModule
    .Injector
    .get(NotificationService)
    .addGeneral({
      type: 'error',
      content: 'Wczytanie sceny nie powiodło się'
    });
}


export interface OverlayUI {
  loadingVisible: boolean;
  loadingDelete: boolean;
  loadingPublic: boolean;
}

export interface OverlayUIState extends EntityState<OverlayUI> {
  showNewOverlayForm: boolean;
  loadingNew: boolean;
}
