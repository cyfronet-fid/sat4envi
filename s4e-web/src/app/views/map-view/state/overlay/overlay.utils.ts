import { Overlay, UIOverlay } from './overlay.model';
import { getBaseUrlAndParamsFrom } from '../../view-manager/overlay-list-modal/wms-url.utils';
import {ImageWMS, TileWMS} from 'ol/source';
import {InjectorModule} from '../../../../common/injector.module';
import {TileLoader} from '../utils/layers-loader.util';
import {NgxUiLoaderService} from 'ngx-ui-loader';
import {Tile} from 'ol/layer';

export function convertToUIOverlay(
  overlay: Overlay,
  active: boolean = false
): UIOverlay {
  const source = getTileWmsFrom(overlay);
  handleLoadingOf(source);
  return {
    favourite: false,
    ...overlay,
    olLayer: new Tile({ source }),
    cid: overlay.id,
    active: active,
    isLoading: false,
    isFavouriteLoading: false
  };
}

export function getImageWmsFrom(overlay: Partial<Overlay>) {
  const urlAndParams = getBaseUrlAndParamsFrom(overlay);
  const {url, ...urlParams} = !!urlAndParams && urlAndParams || {url: overlay.url};
  return new ImageWMS({
    crossOrigin: 'Anonymous',
    serverType: 'geoserver',
    url,
    params: urlParams
  });
}

export function getTileWmsFrom(overlay: Partial<Overlay>) {
  const urlAndParams = getBaseUrlAndParamsFrom(overlay);
  const {url, ...urlParams} = !!urlAndParams && urlAndParams || {url: overlay.url};
  return new TileWMS({
    crossOrigin: 'Anonymous',
    serverType: 'geoserver',
    url,
    params: { TILED: true, ...urlParams}
  });
}

function handleLoadingOf(source: TileWMS) {
  const loaderService = InjectorModule.Injector.get(NgxUiLoaderService);
  const tileLoader = new TileLoader(source);
  tileLoader.start$.then(() => loaderService.startBackground());
}



