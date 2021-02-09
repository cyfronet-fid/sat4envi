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

import {Overlay, UIOverlay} from './overlay.model';
import {ImageWMS, TileWMS} from 'ol/source';
import {InjectorModule} from '../../../../common/injector.module';
import {TileLoader} from '../utils/layers-loader.util';
import {NgxUiLoaderService} from 'ngx-ui-loader';
import {Tile} from 'ol/layer';
import {UrlParser} from '../../../../components/overlay-list/wms-url.utils';

export function convertToUIOverlay(
  overlay: Overlay,
  active: boolean = false
): UIOverlay {
  const source = getTileWmsFrom(overlay);
  handleLoadingOf(source);
  return {
    favourite: false,
    ...overlay,
    olLayer: new Tile({source}),
    cid: overlay.id,
    active: active,
    isLoading: false,
    isFavouriteLoading: false
  };
}

export function getImageWmsFrom(overlay: Partial<Overlay>) {
  const urlParser = new UrlParser(overlay.url);
  return new ImageWMS({
    crossOrigin: 'Anonymous',
    serverType: 'geoserver',
    url: urlParser.getUrlBase(),
    params: urlParser.getParamsWithValues()
  });
}

export function getTileWmsFrom(overlay: Partial<Overlay>) {
  const urlParser = new UrlParser(overlay.url);
  urlParser.remove('server', 'service', 'request', 'version');
  return new TileWMS({
    crossOrigin: 'Anonymous',
    serverType: 'geoserver',
    url: urlParser.getUrlBase(),
    params: {TILED: true, ...urlParser.getParamsWithValues()}
  });
}

function handleLoadingOf(source: TileWMS) {
  const loaderService = InjectorModule.Injector.get(NgxUiLoaderService);
  const tileLoader = new TileLoader(source);
  tileLoader.start$.then(() => loaderService.startBackground());
}
