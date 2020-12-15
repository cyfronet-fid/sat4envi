import {from, Observable, Subscriber, throwError} from 'rxjs';
import { getImageXhr } from 'src/app/views/settings/manage-institutions/institution-form/files.utils';
import ImageWrapper from 'ol/Image';
import WMSCapabilities from 'ol/format/WMSCapabilities';
import {getImageWmsFrom} from '../../views/map-view/state/overlay/overlay.utils';
import Projection from 'ol/proj/Projection';
import {catchError, map, switchMap, tap} from 'rxjs/operators';
import {UrlParser} from './wms-url.utils';
import * as url from 'url';

export interface ILayer {
  name: string;
  title: string;
}

export interface CapabilitiesMetadata {
  layers: ILayer[];
  crs: string,
  extent: number[]
}

/**
 * Remove when layer exists in URL LAYERS param
 * Append layer in LAYERS param if don't exists
 * @param url
 * @param layer metadata name of layer fetched from Get Capabilities URL
 * @return url
 */
export function getToggledLayerInUrl(url: string, layer: string): string {
  const urlParser = new UrlParser(url);
  urlParser.hasValue('layers', layer)
    ? urlParser.removeValues('layers', layer)
    : urlParser.addValues('layers', layer);

  return urlParser.getFullUrl();
}

/**
 * Convert any URL into Get Capabilities link and transform into:
 * layers, crs, extent of image
 * @param url
 * @throws Error on CORS and status code of response other than 200
 */
export function fetchCapabilitiesMetadata$(url: string): Observable<CapabilitiesMetadata> {
  return layerMetadataFrom$(url)
    .pipe(
      map(layersMetadata => {
        const {crs, extent, ...rest} = layersMetadata.BoundingBox[0];
        const layers = unpackLayers(layersMetadata)
            .filter(layer => !!layer.Name)
            .map(layer => ({name: layer.Name, title: layer.Title}));
        return {layers, crs, extent};
      })
    );
}

/**
 * Observable is used due to not working Open Layers Event Dispatcher
 * and force state changes
 */
export function validateImage$(url, crs, extent) {
  return new Observable<string | null>(observer$ => {
    const urlParser = new UrlParser(url);
    if (!urlParser.has('styles')) {
      urlParser.addValues('styles', '');
      url = urlParser.getFullUrl();
    }

    const source = getImageWmsFrom({url});
    source.setImageLoadFunction(getImageWmsLoader(observer$));
    source
      .getImage(extent, 1,1, new Projection({code: crs}))
      .load();
  })
}


function layerMetadataFrom$(url: string) {
  const capabilitiesUrl = getCapabilitiesUrlFrom(url);
  return from(fetch(capabilitiesUrl))
    .pipe(
      tap(response => {
        if (response.status !== 200) {
          throwError(getErrorMessageBy(response.status));
        }
      }),
      switchMap(response => (response as any).text()),
      map((responseText: any) => (new WMSCapabilities()).read(responseText)),
      map(parsedCapabilities => parsedCapabilities.Capability.Layer)
    );
}

function getCapabilitiesUrlFrom(url: string) {
  const isGetCapabilityUrl = !!url.match(/request=GetCapabilities/i);
  if (isGetCapabilityUrl) {
    return url;
  }

  const hasUrlQueryParams = url.includes('?');
  const baseUrl = hasUrlQueryParams ? url.split('?')[0] : url;
  return `${baseUrl}?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities`;
}

function unpackLayers(layer: any, depth = 0) {
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
      .map(layer => unpackLayers(layer, depth + 1))
      .reduce((finalLayers, layers) => finalLayers = [...finalLayers, ...layers])
  ];
}

//////////////////////////////////////////////
// Image loader
//////////////////////////////////////////////

function getImageWmsLoader(getValidUrl$: Subscriber<string>) {
  return (image: ImageWrapper, src: string) => {
    handleBrowserEncodingError(image, getValidUrl$);

    const decodedUrl = decodeURIComponent(src);
    const urlParser = (new UrlParser(decodedUrl));
    urlParser.setValues('WIDTH', '30');
    urlParser.setValues('HEIGHT', '50');
    const imgXhr = getImageXhr(urlParser.getFullUrl());
    const xhr = getHandledXhr(imgXhr, getValidUrl$);
    xhr.onloadend = () => {
      getValidUrl$.next(decodedUrl);
      getValidUrl$.complete();
    };

    return xhr;
  };
}

function getHandledXhr(xhr: XMLHttpRequest, getValidUrl$: Subscriber<string>) {
  xhr.onload = () => {
    handleHttpError(xhr, getValidUrl$);
    handleHttpResponseImageTypeError(xhr, getValidUrl$);
  }
  xhr.onerror = () => handleHttpCorsAndOtherErrors(xhr, getValidUrl$);

  xhr.send();
  return xhr;
}

function handleHttpCorsAndOtherErrors(xhr: XMLHttpRequest, getValidUrl$: Subscriber<string>) {
  if (xhr.response.byteLength === 0) {
    getValidUrl$.error(`
      Cross-Origin Request Blocked:
      Polityka administracyjna serwera nie zezwala na czytanie przez źródła zewnętrzne
    `);

    return;
  }

  getValidUrl$.error(`Wystąpił nieznany błąd o statusie: ${xhr.status}`);
}

function handleBrowserEncodingError(image: ImageWrapper, getValidUrl$: Subscriber<string>) {
  const browserLackEncodeBase64 = typeof window.btoa !== 'function';
  if (browserLackEncodeBase64) {
    image
      .getImage()
      .onerror = () => getValidUrl$.error('Wystąpił błąd enkodowania obrazu WMS');
  }
}

function handleHttpError(xhr: XMLHttpRequest, getValidUrl$: Subscriber<string>) {
  const errorMessage = getErrorMessageBy(xhr.status);
  if (!!errorMessage) {
    getValidUrl$.error(errorMessage + ', sprawdź poprawność URL');
  }
}

function handleHttpResponseImageTypeError(xhr: XMLHttpRequest, getValidUrl$: Subscriber<string>) {
  const contentType = xhr.getResponseHeader('content-type');
  if (!contentType) {
    getValidUrl$.error(`Odpowiedź serwera nie jest zdjęciem, sprawdź poprawność URL!`);
    return;
  }

  const isImage = contentType.indexOf('image') > -1;

  const isInvalidImage = xhr.status === 200 && !isImage;
  if (isInvalidImage) {
    getValidUrl$.error(`Odpowiedź serwera nie jest zdjęciem, sprawdź poprawność URL!`);
  }
}

function getErrorMessageBy(statusCode: number): string | null {
  switch(statusCode) {
    /* Client error codes */
    case 400:
      return `
        (kod 400) Nieprawidłowe zapytanie
        - żądanie nie może być obsłużone przez serwer z
        powodu nieprawidłowości postrzeganej jako błąd użytkownika
      `;
    case 401:
      return `
        (kod 401) Nieautoryzowany dostęp
        - żądanie zasobu, który wymaga uwierzytelnienia
      `;
    case 403:
      return `
        (kod 403) Zabroniony
        - serwer zrozumiał zapytanie,
        lecz konfiguracja bezpieczeństwa zabrania mu
        zwrócić żądany zasób
      `;
    case 404:
      return `
        (kod 404) Nie znaleziono
        - serwer nie odnalazł zasobu według podanego URL
        ani niczego co by wskazywało na istnienie
        takiego zasobu w przeszłości
      `;

    /* Server HTTP errors codes */
    case 500:
      return `
        (Kod 500) Wewnętrzny błąd serwera
        - serwer napotkał niespodziewane trudności,
        które uniemożliwiły zrealizowanie żądania
      `;
    case 502:
      return `
        (Kod 502) Błąd bramy - serwer
        - spełniający rolę bramy lub pośrednika
        - otrzymał niepoprawną odpowiedź od serwera nadrzędnego
        i nie jest w stanie zrealizować żądania klienta
      `;
    case 503:
      return `
        (Kod 503) Usługa niedostępna
        - serwer nie jest w stanie w danej chwili zrealizować
        zapytania klienta ze względu na przeciążenie
      `;
  }

  if (statusCode > 400 && statusCode < 500) {
    return `(Kod ${statusCode}) Nieznany kod błędu aplikacji klienta`;
  }

  if (statusCode > 500 && statusCode < 600) {
    return `(Kod ${statusCode}) Nieznany kod błędu serwera HTTP`;
  }

  return null;
}
