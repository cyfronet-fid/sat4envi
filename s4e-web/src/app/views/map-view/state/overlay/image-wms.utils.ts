import { Subscriber } from 'rxjs';
import { getImageXhr } from 'src/app/views/settings/manage-institutions/institution-form/files.utils';
import ImageWrapper from 'ol/Image';


export function getImageWmsLoader(
  error$: Subscriber<string>,
  urlParser: (src: string) => string = (src: string) => src
) {
  return (image: ImageWrapper, src: string) => {
    handleBrowserEncodingError(image, error$);
    return getHandledXhr(getImageXhr(urlParser(src)), error$);
  };
}


function getHandledXhr(xhr: XMLHttpRequest, error$: Subscriber<string>) {
  xhr.onload = () => !handleHttpError(xhr, error$)
    || handleHttpResponseImageTypeError(xhr, error$);
  xhr.onloadend = () => error$.next(null);
  xhr.onerror = () => handleHttpCorsAndOtherErrors(xhr, error$);

  xhr.send();
  return xhr;
}

function handleHttpCorsAndOtherErrors(xhr: XMLHttpRequest, error$: Subscriber<string>) {
  if (xhr.response.byteLength === 0) {
    error$.error(`
      Cross-Origin Request Blocked:
      Polityka administracyjna serwera nie zezwala na czytanie przez źródła zewnętrzne
    `);

    return;
  }

  error$.error(`Wystąpił nieznany błąd o statusie: ${xhr.status}`);
}

function handleBrowserEncodingError(image: ImageWrapper, error$: Subscriber<string>) {
  const browserLackEncodeBase64 = typeof window.btoa !== 'function';
  if (browserLackEncodeBase64) {
    image
      .getImage()
      .onerror = () => error$.error('Wystąpił błąd enkodowania obrazu WMS');
  }
}

function handleHttpError(xhr: XMLHttpRequest, error$: Subscriber<string>): boolean {
  const errorMessage = getErrorMessageBy(xhr.status);
  if (!!errorMessage) {
    error$.error(errorMessage + ', sprawdź poprawność URL');
  }

  return !!errorMessage;
}

function handleHttpResponseImageTypeError(xhr: XMLHttpRequest, error$: Subscriber<string>) {
  const isImage = xhr.getResponseHeader('content-type').indexOf('image') > -1;
  const isInvalidImage = xhr.status === 200 && !isImage;
  if (isInvalidImage) {
    error$.error(`Odpowiedź serwera nie jest zdjęciem, sprawdź poprawność URL!`);
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
