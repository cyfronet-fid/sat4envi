import {from, Observable, Subscriber, throwError} from 'rxjs';
import { getImageXhr } from 'src/app/views/settings/manage-institutions/institution-form/files.utils';
import ImageWrapper from 'ol/Image';
import WMSCapabilities from 'ol/format/WMSCapabilities';
import {getImageWmsFrom} from '../../views/map-view/state/overlay/overlay.utils';
import Projection from 'ol/proj/Projection';
import {catchError, filter, map, switchMap, tap} from 'rxjs/operators';

/**
 * Observable is used due to not working Open Layers Event Dispatcher
 * and force state changes
 */
export function getValidWmsUrl(url: string) {
  return new Observable<string | null>(observer$ => {
    layerMetadataFrom$(url)
      .pipe(
        catchError(error => {
          observer$.error(error);
          observer$.complete();
          return throwError(error);
        }),
        tap(layerMetadata => {
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
        })
      );
  });
}

function appendLayers() {
  
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

//////////////////////////////////////////////
// Image loader
//////////////////////////////////////////////

function getImageWmsLoader(getValidUrl$: Subscriber<string>) {
  return (image: ImageWrapper, src: string) => {
    handleBrowserEncodingError(image, getValidUrl$);

    const decodedUrl = decodeURIComponent(src);
    const urlWithFixedWmsSize = getFixedSizeWmsUrl(decodedUrl, 30, 30);
    const imgXhr = getImageXhr(urlWithFixedWmsSize);
    const xhr = getHandledXhr(imgXhr, getValidUrl$);
    xhr.onloadend = () => {
      getValidUrl$.next(decodedUrl);
      getValidUrl$.complete();
    };

    return xhr;
  };
}

function getFixedSizeWmsUrl(url: string, width: number, height: number) {
  const urlBase = url.split('?')[0];
  const urlQueryParams = url.split('?')[1];
  const urlParamsWithoutSize = urlQueryParams
    .replace(/(&?)width=[0-9]+/i, '')
    .replace(/(&?)height=[0-9]+/i, '');
  return `${urlBase}?${urlParamsWithoutSize}&width=${width}&height=${height}`
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
